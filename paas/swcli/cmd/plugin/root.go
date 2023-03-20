package plugin

import (
	"github.com/spf13/cobra"
	"gitlab.alibaba-inc.com/pe3/swcli/lib"
)

var subCommand = &cobra.Command{
	Use:   "plugin",
	Short: "Manage plugins in appmanager server",
}

func Init(root *cobra.Command) {
	initUploadCommand()
	initOperateCommand()

	root.AddCommand(subCommand)
	subCommand.AddCommand(uploadCommand)
	subCommand.AddCommand(operateCommand)

	subCommand.SetHelpFunc(func(command *cobra.Command, strings []string) {
		command.Println(command.UsageString())
		lib.Exit()
	})
}
