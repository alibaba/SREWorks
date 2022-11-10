package plugin

import (
	"github.com/rs/zerolog/log"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"gitlab.alibaba-inc.com/pe3/swcli/lib"
	"gitlab.alibaba-inc.com/pe3/swcli/svc/appmanagerv1"
)

func initUploadCommand() {
	uploadCommand.Flags().StringP("filepath", "", "", "zip file")
	uploadCommand.Flags().BoolP("overwrite", "", true, "whether to overwrite the exist plugin")
	uploadCommand.Flags().BoolP("enable", "", false, "enable the plugin right now")
	uploadCommand.MarkFlagRequired("filepath")
	viper.BindPFlag("plugin.upload.filepath", uploadCommand.Flags().Lookup("filepath"))
	viper.BindPFlag("plugin.upload.overwrite", uploadCommand.Flags().Lookup("overwrite"))
	viper.BindPFlag("plugin.upload.enable", uploadCommand.Flags().Lookup("enable"))
}

var uploadCommand = &cobra.Command{
	Use:   "upload",
	Short: "upload plugin to appmanager server",
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

		filepath := viper.GetString("plugin.upload.filepath")
		overwrite := viper.GetBool("plugin.upload.overwrite")
		enable := viper.GetBool("plugin.upload.enable")
		response, err := server.UploadPlugin(filepath, overwrite, enable)
		if response != nil {
			log.Info().Msgf("plugin has uploaded to appmanager")
		} else {
			log.Error().Err(err).Msgf("upload plugin failed")
			lib.Exit()
		}
	},
}
