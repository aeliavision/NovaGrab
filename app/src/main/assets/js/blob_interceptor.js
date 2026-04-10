(function() {
    'use strict';

    if (window.__SmartVDL_BlobInterceptorInstalled) return;
    window.__SmartVDL_BlobInterceptorInstalled = true;

    const bridge = window.SmartVDL;
    if (!bridge) return;

    function reportBlob(url, mimeType) {
        try {
            if (bridge.onBlobVideoDetected && typeof bridge.onBlobVideoDetected === 'function') {
                bridge.onBlobVideoDetected(String(url || ''), String(mimeType || ''), 0, 0);
            }
        } catch (err) {}
    }

    // 1) Hook URL.createObjectURL so we can observe blob: URLs being produced.
    try {
        const originalCreateObjectURL = URL.createObjectURL;
        URL.createObjectURL = function(obj) {
            const result = originalCreateObjectURL.apply(this, arguments);
            try {
                if (result && typeof result === 'string' && result.startsWith('blob:')) {
                    const type = obj && obj.type ? obj.type : '';
                    reportBlob(result, type);
                }
            } catch (err) {}
            return result;
        };
    } catch (err) {}

    // 2) Watch <video> src/currentSrc changes for blob URLs.
    function scanForBlobVideos() {
        try {
            document.querySelectorAll('video').forEach(v => {
                const src = v.currentSrc || v.src;
                if (src && typeof src === 'string' && src.startsWith('blob:')) {
                    reportBlob(src, '');
                }
            });
        } catch (err) {}
    }

    // Initial + reactive
    scanForBlobVideos();
    try {
        const obs = new MutationObserver(() => scanForBlobVideos());
        obs.observe(document.documentElement, { childList: true, subtree: true, attributes: true, attributeFilter: ['src'] });
    } catch (err) {}
})();
