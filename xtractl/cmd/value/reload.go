package value

import (
	"fmt"
	"strings"

	"github.com/interactive-instruments/xtraplatform-cli/xtractl/client"
	"github.com/spf13/cobra"
)

var ids []string

// Cmd represents the reload command
func reloadCmd(api client.Client, name string, debug *bool) *cobra.Command {
	cmd := &cobra.Command{
		Use:     "reload paths...",
		Short:   "Reload value configuration",
		Long:    "Reload value configuration\n\nRereads all configuration files that are relevant for the given values.\nIf effective changes to a value configuration are detected, the value is reloaded.",
		Example: name + " value reload \"*\"\n" + name + " value reload codelists\n" + name + " value reload codelists/foo,codelists/bar",
		Args: func(cmd *cobra.Command, args []string) error {
			if len(args) == 0 {
				return fmt.Errorf("no paths given, at least one value path or \"*\" is required\n")
			}

			for _, id := range strings.Split(args[0], ",") {
				if strings.TrimSpace(id) != "*" {
					ids = append(ids, strings.TrimSpace(id))
				}
			}

			return nil
		},
		PreRunE: func(cmd *cobra.Command, args []string) error {
			for _, id := range ids {
				if !isValidId(id, ValueTypes) {
					return fmt.Errorf("unknown value path: %s\n", id)
				}
			}

			return nil
		},
		Run: func(cmd *cobra.Command, args []string) {
			err := api.ReloadValue(ids)

			if err != nil {
				fmt.Println(err)
			}
		},
	}

	return cmd
}

func isValidId(id string, types []string) bool {
	if id == "*" {
		return true
	}

	for _, typ := range types {
		if strings.HasPrefix(id, typ) {
			return true
		}
	}

	return false
}
