package gg.rohan.beak.pterodactyl

import com.google.gson.annotations.SerializedName

internal enum class PowerState {

    START,
    STOP,
    RESTART,
    KILL,
    ;

    override fun toString(): String {
        return name.lowercase()
    }
}

internal class PowerStateBody(@SerializedName("signal") internal val signal: String) {
    constructor(state: PowerState): this(state.toString().lowercase())
}