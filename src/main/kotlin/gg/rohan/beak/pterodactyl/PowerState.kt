package gg.rohan.beak.pterodactyl

enum class PowerState {

    START,
    STOP,
    RESTART,
    KILL,
    ;

    override fun toString(): String {
        return name.lowercase()
    }
}