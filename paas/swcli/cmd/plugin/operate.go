package plugin

import (
	"github.com/rs/zerolog/log"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"gitlab.alibaba-inc.com/pe3/swcli/lib"
	"gitlab.alibaba-inc.com/pe3/swcli/svc/appmanagerv1"
)

func initOperateCommand() {
	operateCommand.Flags().StringP("name", "", "", "plugin name")
	operateCommand.Flags().StringP("version", "", "", "plugin version")
	operateCommand.Flags().StringP("operation", "", "", "plugin operation")
	operateCommand.MarkFlagRequired("name")
	operateCommand.MarkFlagRequired("version")
	operateCommand.MarkFlagRequired("operation")
	viper.BindPFlag("plugin.operate.name", operateCommand.Flags().Lookup("name"))
	viper.BindPFlag("plugin.operate.version", operateCommand.Flags().Lookup("version"))
	viper.BindPFlag("plugin.operate.operation", operateCommand.Flags().Lookup("operation"))
}

var operateCommand = &cobra.Command{
	Use:   "operate",
	Short: "send operation command to appmanager server",
	Run: func(cmd *cobra.Command, args []string) {
		endpoint := viper.GetString("endpoint")
		clientId := viper.GetString("client-id")
		clientSecret := viper.GetString("client-secret")
		username := viper.GetString("username")
		password := viper.GetString("password")
		server := appmanagerv1.NewAppManagerServer(
			lib.NewClient(endpoint, clientId, clientSecret, username, password),
			endpoint,
		)

		name := viper.GetString("plugin.operate.name")
		version := viper.GetString("plugin.operate.version")
		operation := viper.GetString("plugin.operate.operation")
		if len(name) == 0 || len(version) == 0 || len(operation) == 0 {
			log.Error().Msgf("invalid arguments")
			lib.Exit()
		}
		if operation != "enable" && operation != "disable" {
			log.Error().Msgf("invalid operation arguments")
			lib.Exit()
		}
		response, err := server.OperatePlugin(name, version, operation)
		if response != nil {
			log.Info().Msgf("the operation has sent to appmanager")
		} else {
			log.Error().Err(err).Msgf("failed to send operation command to appmanager")
			lib.Exit()
		}
	},
}
