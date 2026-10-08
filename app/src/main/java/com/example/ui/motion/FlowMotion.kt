package com.example.ui.motion

import android.content.Context
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

/**
 * FlowModes Central Motion System
 *
 * Defines consistent, physical, spring-based motion specifications,
 * intensity levels, tactile press behaviors, and restrained error shakes.
 *
 * Principles:
 * - Real spring physics with subtle overshoot (never cartoonish)
 * - GraphicsLayer transforms to avoid layout recalculations (120 FPS targets)
 * - Tactile weight on presses and releases
 * - Zero continuous background animation loops
 */
object FlowMotion {

    // === Motion Intensity Levels ===
    enum class Level(
        val pressScale: Float,
        val releaseOvershoot: Float
    ) {
        /** Level 1: Ordinary list items, settings rows, navigation icons */
        SUBTLE(pressScale = 0.982f, releaseOvershoot = 1.01f),

        /** Level 2: Buttons, cards, toggles, mode switching */
        MEDIUM(pressScale = 0.968f, releaseOvershoot = 1.02f),

        /** Level 3: Major success states, mode activation, FAB / primary CTA */
        EXPRESSIVE(pressScale = 0.952f, releaseOvershoot = 1.035f)
    }

    // === Spring & Animation Specifications ===

    /** Press down: Fast, responsive compression without bounce */
    val PressIn: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** Press release: Soft physical return with tiny, controlled overshoot */
    val PressRelease: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.68f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** General-purpose balanced spring */
    val SpringDefault: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.74f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Softer settling spring for spacious elements and dialogs */
    val SoftSpring: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessLow
    )

    /** Controlled bouncy spring for active icons, checkmarks, and badges */
    val Bouncy: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.58f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Snappy spring for quick responsive actions */
    val Snappy: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.80f,
        stiffness = Spring.StiffnessMedium
    )

    /** Switch toggle movement: slight acceleration, tiny overshoot, smooth settle */
    val Toggle: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.64f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Switch toggle movement for Dp offsets */
    val ToggleDp: FiniteAnimationSpec<Dp> = spring(
        dampingRatio = 0.64f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Bottom sheet entrance/exit specification */
    val BottomSheet: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.80f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Page transition entering specification (~210ms) */
    val PageEnter = tween<Float>(
        durationMillis = 210,
        easing = FastOutSlowInEasing
    )

    /** Page transition entering offset specification (~210ms) */
    val PageEnterOffset: FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = 210,
        easing = FastOutSlowInEasing
    )

    /** Page transition exiting specification (~190ms) */
    val PageExit = tween<Float>(
        durationMillis = 190,
        easing = LinearOutSlowInEasing
    )

    /** Page transition exiting offset specification (~190ms) */
    val PageExitOffset: FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = 190,
        easing = LinearOutSlowInEasing
    )

    /** Success pop animation specification */
    val Success: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.60f,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Snap back spring for rubber overscroll and released sliders */
    val SnapBack: FiniteAnimationSpec<Float> = spring(
        dampingRatio = 0.70f,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Checks if reduced motion is requested by system accessibility settings.
     */
    fun isReducedMotionEnabled(context: Context): Boolean {
        return try {
            val durationScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            durationScale == 0f
        } catch (_: Exception) {
            false
        }
    }
}

/**
 * Tactile Press Modifier
 *
 * Applies a physical compression on press and a soft spring overshoot on release.
 * Evaluated exclusively in the graphicsLayer (draw phase), avoiding recomposition
 * and relayout of children for maximum frame rate (120/90/60 FPS).
 */
fun Modifier.flowPress(
    level: FlowMotion.Level = FlowMotion.Level.SUBTLE,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    if (!enabled) return@composed this
    val context = LocalContext.current
    val reducedMotion = remember(context) { FlowMotion.isReducedMotionEnabled(context) }
    if (reducedMotion) return@composed this

    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()

    val targetScale = if (isPressed) level.pressScale else 1.0f

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = if (isPressed) FlowMotion.PressIn else FlowMotion.PressRelease,
        label = "flowPressScale"
    )

    this.graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }
}

/**
 * Restrained Error / Validation Shake Modifier
 *
 * Shakes the control horizontally along the X-axis by ~4-5dp and settles cleanly.
 * Amplitude is kept restrained (never shakes entire screens).
 */
fun Modifier.flowShake(trigger: Any?): Modifier = composed {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger != null && trigger != 0 && trigger != false) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 280
                    0f at 0
                    (-5f) at 50
                    5f at 100
                    (-3f) at 150
                    3f at 200
                    (-1f) at 240
                    0f at 280
                }
            )
        }
    }

    this.graphicsLayer {
        translationX = shakeOffset.value
    }
}

/**
 * FlowHaptics: Lightweight, synchronized tactile feedback helpers.
 * Respects system haptic feedback and executes at the exact point of interaction.
 */
object FlowHaptics {

    /** Light tick for switches, tab clicks, filter toggles */
    fun tick(hapticFeedback: HapticFeedback, view: android.view.View? = null) {
        try {
            if (view != null) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } else {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } catch (_: Exception) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /** Satisfying press feedback for mode activation, run execution, and successful saves */
    fun impact(hapticFeedback: HapticFeedback, view: android.view.View? = null) {
        try {
            if (view != null) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            } else {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (_: Exception) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /** Success confirmation feedback */
    fun success(hapticFeedback: HapticFeedback, view: android.view.View? = null) {
        impact(hapticFeedback, view)
    }

    /** Light selection change feedback */
    fun selection(hapticFeedback: HapticFeedback, view: android.view.View? = null) {
        tick(hapticFeedback, view)
    }

    /** Mode toggling feedback with distinct feel for activation vs deactivation */
    fun modeToggle(hapticFeedback: HapticFeedback, view: android.view.View? = null, willBeActive: Boolean) {
        if (willBeActive) {
            impact(hapticFeedback, view)
        } else {
            tick(hapticFeedback, view)
        }
    }

    /** Error shake feedback */
    fun error(hapticFeedback: HapticFeedback, view: android.view.View? = null) {
        try {
            if (view != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            } else {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (_: Exception) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
