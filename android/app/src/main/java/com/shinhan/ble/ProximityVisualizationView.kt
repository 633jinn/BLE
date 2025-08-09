package com.shinhan.ble

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.shinhan.ble.data.ScannedUser
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class ProximityVisualizationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val centerPaint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val userPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val ripplePaint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 3f
        alpha = 100
    }

    private var scannedUsers = mutableListOf<ScannedUser>()
    private var centerX = 0f
    private var centerY = 0f
    private var maxRadius = 0f
    private val centerRadius = 20f
    private val userRadius = 15f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        maxRadius = min(w, h) / 2f - 50f // Leave some margin
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas ?: return

        // Draw concentric circles for proximity zones
        drawProximityZones(canvas)

        // Draw center user (self)
        canvas.drawCircle(centerX, centerY, centerRadius, centerPaint)

        // Draw scanned users around the center
        drawScannedUsers(canvas)
    }

    private fun drawProximityZones(canvas: Canvas) {
        val zones = 4
        for (i in 1..zones) {
            val radius = (maxRadius / zones) * i
            ripplePaint.alpha = (100 / i).coerceAtLeast(20)
            canvas.drawCircle(centerX, centerY, radius, ripplePaint)
        }
    }

    private fun drawScannedUsers(canvas: Canvas) {
        for (user in scannedUsers) {
            val distance = ScannedUser.getRelativeDistance(user.rssi)
            val userDistance = distance * maxRadius

            // Generate consistent angle based on device address
            val angle = (user.deviceAddress.hashCode().toDouble() % 360) * Math.PI / 180

            val userX = centerX + (cos(angle) * userDistance).toFloat()
            val userY = centerY + (sin(angle) * userDistance).toFloat()

            // Ensure user is within bounds
            val clampedX = userX.coerceIn(userRadius, width - userRadius)
            val clampedY = userY.coerceIn(userRadius, height - userRadius)

            userPaint.color = user.color
            canvas.drawCircle(clampedX, clampedY, userRadius, userPaint)
        }
    }

    fun updateUsers(users: List<ScannedUser>) {
        scannedUsers.clear()
        scannedUsers.addAll(users)
        invalidate()
    }

    fun clearUsers() {
        scannedUsers.clear()
        invalidate()
    }
}