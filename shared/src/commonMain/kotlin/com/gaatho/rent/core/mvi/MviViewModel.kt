package com.gaatho.rent.core.mvi

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.OrbitContainerHost

/**
 * Base class for all ViewModels in this app that follow the MVI pattern via Orbit MVI.
 *
 * ## Type Parameters
 * - [STATE] — Immutable data class representing the full UI state. The UI derives
 *   all its rendering from this object. Annotate with [@Immutable] or [@Stable].
 * - [SIDE_EFFECT] — Sealed interface for **one-time** events that are not part of
 *   persistent state: navigation commands, Snackbars, dialogs. These are emitted
 *   via `postSideEffect {}` and consumed exactly once by the UI.
 * - [ACTION] — Sealed interface for all user interactions. The UI dispatches actions
 *   through the single [onAction] entry point.
 *
 * ## Why `ContainerHost<STATE, SIDE_EFFECT>` not `OrbitContainerHost<S, S, SE>`
 * `ContainerHost` is the stable, public Orbit API (2 type params). `OrbitContainerHost`
 * is an internal 3-param interface (`INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT`)
 * that `ContainerHost` delegates to. Coding against the public API makes upgrades safer.
 *
 * ## Orbit Patterns Enforced
 * - Initial work is done in `orbitContainer { onCreate }`, never in `init {}`
 * - All state mutations happen inside `reduce {}`
 * - One-time events use `postSideEffect {}`
 * - Long-lived flows use `intent(registerIdling = false) { collect {} }`
 * - The [onAction] function is the ONLY public entry point for UI events
 */
abstract class MviViewModel<STATE : Any, SIDE_EFFECT : Any, ACTION : Any> :
    OrbitContainerHost<STATE, STATE, SIDE_EFFECT>, ViewModel() {

    /**
     * The single entry point for all UI events. Dispatches to private
     * `intent {}` handler functions — never called recursively.
     */
    abstract fun onAction(action: ACTION)
}
