package com.shikshapulse.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.SystemClock
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class ReelTrackingAccessibilityService : AccessibilityService() {
    private var blockerView: FrameLayout? = null
    private var lastSwipeAt = 0L
    private val preferencesListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == null || key == "reels_block_enabled") {
            if (!ReelTrackingPreferences.isBlockEnabled(this)) removeBlocker()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).registerOnSharedPreferenceChangeListener(preferencesListener)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName?.toString() != INSTAGRAM_PACKAGE) {
            removeBlocker()
            return
        }

        val root = rootInActiveWindow ?: return
        val isReels = try {
            hasReelsViewport(root)
        } finally {
            root.recycle()
        }

        if (!isReels) {
            removeBlocker()
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastSwipeAt >= SWIPE_DEBOUNCE_MS) {
                ReelTrackingPreferences.recordSwipe(this, System.currentTimeMillis())
                lastSwipeAt = now
            }
        }

        if (ReelTrackingPreferences.isBlockEnabled(this)) {
            showBlocker()
        } else {
            removeBlocker()
        }
    }

    override fun onInterrupt() {
        removeBlocker()
    }

    override fun onDestroy() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(preferencesListener)
        removeBlocker()
        super.onDestroy()
    }

    private fun hasReelsViewport(root: AccessibilityNodeInfo): Boolean {
        val nodes = ArrayDeque<AccessibilityNodeInfo>()
        nodes.add(root)
        var inspected = 0
        while (nodes.isNotEmpty() && inspected < MAX_INSPECTED_NODES) {
            val node = nodes.removeFirst()
            inspected++
            val viewId = node.viewIdResourceName.orEmpty().lowercase()
            val isReelContainer = viewId.contains("reel") || viewId.contains("clips_viewer")
            if (isReelContainer) {
                nodes.forEach { it.recycle() }
                return true
            }
            for (index in 0 until node.childCount) {
                node.getChild(index)?.let(nodes::addLast)
            }
            if (node !== root) node.recycle()
        }
        nodes.forEach { it.recycle() }
        return false
    }

    private fun showBlocker() {
        if (blockerView != null) return
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.rgb(24, 35, 61))
        }
        content.addView(
            TextView(this).apply {
                text = "Reels paused"
                textSize = 30f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        )
        content.addView(
            TextView(this).apply {
                text = "Your focus time is yours. Head back when you're ready."
                textSize = 16f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                setPadding(0, 18, 0, 28)
            },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        )
        content.addView(
            Button(this).apply {
                text = "Leave Reels"
                setOnClickListener { performGlobalAction(GLOBAL_ACTION_BACK) }
            },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        )
        val overlay = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(24, 35, 61))
            isClickable = true
            addView(
                content,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            )
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        (getSystemService(WINDOW_SERVICE) as WindowManager).addView(overlay, params)
        blockerView = overlay
    }

    private fun removeBlocker() {
        val view = blockerView ?: return
        blockerView = null
        (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(view)
    }

    private companion object {
        const val SWIPE_DEBOUNCE_MS = 800L
        const val MAX_INSPECTED_NODES = 400
    }
}
