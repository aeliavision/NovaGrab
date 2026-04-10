(function() {
    'use strict';

    if (window.__SmartVDL_LongPressInstalled) return;
    window.__SmartVDL_LongPressInstalled = true;

    const bridge = window.SmartVDL;
    if (!bridge || typeof bridge.onVideoLongPressed !== 'function') return;

    let longPressTimer = null;

    function clear() {
        if (longPressTimer) {
            clearTimeout(longPressTimer);
            longPressTimer = null;
        }
    }

    document.addEventListener('touchstart', (e) => {
        const target = e.target && e.target.closest ? e.target.closest('video') : null;
        if (!target) return;

        clear();
        longPressTimer = setTimeout(() => {
            try {
                const url = target.currentSrc || target.src;
                if (url) {
                    bridge.onVideoLongPressed(url, target.videoWidth || 0, target.videoHeight || 0);
                }
            } catch (err) {}
        }, 600);
    }, { passive: true });

    document.addEventListener('touchend', clear, { passive: true });
    document.addEventListener('touchmove', clear, { passive: true });
})();
