package commands

class AllCommands {
    companion object {
        val commands = listOf(
            PingCmd(),
            ChooseCmd(),
            OneVOneCmd(),
            ChatDeadCmd(),
            HugCmd(),
            KissCmd(),
            CuddleCmd(),
            HandHoldCmd(),
            HeadPatCmd(),
            BoopCmd(),
            HelpCmd(),
            UpdateStatusCmd(),
        )
    }
}