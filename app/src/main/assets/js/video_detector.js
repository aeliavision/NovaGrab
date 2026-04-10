(function() {
    'use strict';

    const bridge = window.SmartVDL;
    if (!bridge) return;

    const reportedUrls = new Set();

    function findNearestHeading(el) {
        if (!el) return (document.title || '').trim();

        const headings = Array.from(document.querySelectorAll('h1, h2, h3, h4, h5, h6, .title, .video-title'));
        let bestHeading = null;
        
        for (let i = 0; i < headings.length; i++) {
            const heading = headings[i];
            // Node.DOCUMENT_POSITION_FOLLOWING is 4.
            // If heading precedes el, el follows heading.
            if (heading.compareDocumentPosition(el) & Node.DOCUMENT_POSITION_FOLLOWING) {
                bestHeading = heading;
            } else {
                break;
            }
        }
        
        if (bestHeading && bestHeading.textContent && bestHeading.textContent.trim()) {
            return bestHeading.textContent.trim();
        }
        
        if (document.title && document.title.trim()) {
            return document.title.trim();
        }
        return '';
    }

    function reportVideo(url, type, width, height, title) {
        if (!url || url.length < 10) return;
        if (reportedUrls.has(url)) return;
        reportedUrls.add(url);

        try {
            bridge.onVideoDetected(
                url,
                type || 'unknown',
                width || 0,
                height || 0,
                title || ''
            );
        } catch(e) {}
    }

    function scanDom() {
        document.querySelectorAll('video').forEach(v => {
            const title = findNearestHeading(v);
            if (v.src) reportVideo(v.src, 'video/mp4', v.videoWidth, v.videoHeight, title);
            v.querySelectorAll('source').forEach(s => {
                if (s.src) reportVideo(s.src, s.type, 0, 0, title);
            });
        });

        document.querySelectorAll('source[src]').forEach(s => {
            const title = findNearestHeading(s);
            if (s.src) reportVideo(s.src, s.type, 0, 0, title);
        });

        document.querySelectorAll('iframe[src]').forEach(f => {
            const src = f.src;
            if (/youtube|vimeo|dailymotion|twitch/i.test(src)) {
                bridge.onEmbedDetected(src);
            }
        });
    }

    const originalMediaSource = window.MediaSource;
    if (originalMediaSource) {
        class InterceptedMediaSource extends originalMediaSource {
            constructor() {
                super();
                bridge.onMseDetected('MediaSource created');
            }
            addSourceBuffer(mimeType) {
                bridge.onMseSourceBuffer(mimeType);
                return super.addSourceBuffer(mimeType);
            }
        }
        try {
            window.MediaSource = InterceptedMediaSource;
        } catch(e) {}
    }

    const originalFetch = window.fetch;
    window.fetch = function(...args) {
        const url = args[0]?.url || args[0] || '';
        if (typeof url === 'string') {
            if (/\.(m3u8|mpd|mp4)(\?|$)/i.test(url)) {
                reportVideo(url, detectType(url), 0, 0, document.title || '');
            }
        }
        return originalFetch.apply(this, args).then(function(response) {
            try {
                const resUrl = response && response.url ? response.url : url;
                if (resUrl && typeof resUrl === 'string') {
                    const ct = response && response.headers && response.headers.get
                        ? response.headers.get('content-type')
                        : null;
                    if (ct && /video\/|mpegurl|dash\+xml|mp2t/i.test(ct)) {
                        reportVideo(resUrl, ct.split(';')[0].trim(), 0, 0, document.title || '');
                    } else if (/\.(m3u8|mpd|mp4)(\?|$)/i.test(resUrl)) {
                        reportVideo(resUrl, detectType(resUrl), 0, 0, document.title || '');
                    }
                }
            } catch (e) {}
            return response;
        });
    };

    const originalXhrOpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function(method, url, ...rest) {
        if (typeof url === 'string' && /\.(m3u8|mpd|mp4)(\?|$)/i.test(url)) {
            reportVideo(url, detectType(url), 0, 0, document.title || '');
        }
        try {
            const self = this;
            self.addEventListener('readystatechange', function() {
                try {
                    if (self.readyState === 4) {
                        const responseUrl = self.responseURL || url;
                        const ct = self.getResponseHeader ? self.getResponseHeader('content-type') : null;
                        if (ct && /video\/|mpegurl|dash\+xml|mp2t/i.test(ct)) {
                            reportVideo(responseUrl, ct.split(';')[0].trim(), 0, 0, document.title || '');
                        } else if (typeof responseUrl === 'string' && /\.(m3u8|mpd|mp4)(\?|$)/i.test(responseUrl)) {
                            reportVideo(responseUrl, detectType(responseUrl), 0, 0, document.title || '');
                        }
                    }
                } catch (e) {}
            });
        } catch (e) {}
        return originalXhrOpen.apply(this, [method, url, ...rest]);
    };

    let scanDebounceTimer = null;
    function scheduleScan() {
        if (scanDebounceTimer) return;
        scanDebounceTimer = setTimeout(function() {
            scanDebounceTimer = null;
            scanDom();
        }, 500); // Increased debounce to 500ms for better performance
    }

    const observer = new MutationObserver((mutations) => {
        let shouldScan = false;
        for (let i = 0; i < mutations.length; i++) {
            const m = mutations[i];
            if (m.type === 'childList') {
                const added = m.addedNodes;
                for (let j = 0; j < added.length; j++) {
                    const node = added[j];
                    if (node.nodeType === 1) { // Element node
                        const tag = node.tagName;
                        if (tag === 'VIDEO' || tag === 'SOURCE' || tag === 'IFRAME' ||
                            (node.querySelector && node.querySelector('video, source[src], iframe[src]'))) {
                            shouldScan = true;
                            break;
                        }
                    }
                }
            } else if (m.type === 'attributes') {
                const tag = m.target.tagName;
                if (tag === 'VIDEO' || tag === 'SOURCE' || tag === 'IFRAME') {
                    shouldScan = true;
                }
            }
            if (shouldScan) break;
        }
        if (shouldScan) scheduleScan();
    });

    observer.observe(document.documentElement, {
        childList: true,
        subtree: true,
        attributes: true,
        attributeFilter: ['src', 'srcset'] // Only watch for source changes
    });

    function detectType(url) {
        if (/\.m3u8/i.test(url)) return 'application/x-mpegurl';
        if (/\.mpd/i.test(url)) return 'application/dash+xml';
        if (/\.ts/i.test(url)) return 'video/mp2t';
        return 'video/mp4';
    }

    if (document.readyState === 'complete') {
        scanDom();
    } else {
        window.addEventListener('load', scanDom);
        setTimeout(scanDom, 500);
        setTimeout(scanDom, 1500);
        setTimeout(scanDom, 3000);
    }
})();
