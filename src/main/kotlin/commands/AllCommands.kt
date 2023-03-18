package commands

import utilities.KopyCommand

class AllCommands {
    companion object {
        val commands: List<KopyCommand> = listOf(
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
            NoContextCmd(),
            PeopleCmd(),
            PetCmd(),
            MemeCmd(),
            PollCmd(),
//            PhraseCommands(),
            SlashCommandUtilsCmd()
        )
    }
}