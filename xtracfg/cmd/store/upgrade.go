package store

import (
	"errors"
	"fmt"
	"os"
	"strconv"

	"github.com/interactive-instruments/xtraplatform-cli/libxtracfg/go/xtracfg"
	"github.com/interactive-instruments/xtraplatform-cli/xtracfg/util"
	"github.com/spf13/cobra"
)

var backup *bool
var force *bool
var noConfirm *bool
var keepRedundant *bool

// Cmd represents the entity command
func UpgradeCmd(store xtracfg.Store, name string, verbose *bool, debug *bool) *cobra.Command {
	upgrade := &cobra.Command{
		Use:   "upgrade",
		Short: "Upgrade the store source",
		Long: `Upgrade the store source
Executes all subcommands in order, see the subcommand help for details.
No changes are made without confirmation (unless --yes is set).`,
		Args: cobra.NoArgs,
		Run: func(cmd *cobra.Command, args []string) {
			if *debug {
				fmt.Fprint(os.Stdout, "Upgrading store source: ", store.Label(), "\n")
			}

			//TODO: use check without subcommands, add section headers in java

			fmt.Fprint(os.Stdout, "\n", "Upgrading cfg.yml", "\n")

			results, err := store.Handle(map[string]interface{}{"ignoreRedundant": strconv.FormatBool(*keepRedundant)}, "pre_upgrade", "cfg")

			if !*noConfirm {
				util.PrintResults(results, err)
			}

			if xtracfg.HasStatus(results, xtracfg.Confirmation) {
				results, err = store.Handle(map[string]interface{}{"backup": strconv.FormatBool(*backup), "ignoreRedundant": strconv.FormatBool(*keepRedundant), "noConfirm": strconv.FormatBool(*noConfirm)}, "upgrade", "cfg")

				util.PrintResults(results, err)
			}

			fmt.Fprint(os.Stdout, "\n", "Upgrading entities", "\n")

			results, err = store.Handle(map[string]interface{}{"force": strconv.FormatBool(*force), "ignoreRedundant": strconv.FormatBool(*keepRedundant)}, "pre_upgrade", "entities")

			if !*noConfirm {
				util.PrintResults(results, err)
			}

			if xtracfg.HasStatus(results, xtracfg.Confirmation) {
				results, err = store.Handle(map[string]interface{}{"backup": strconv.FormatBool(*backup), "force": strconv.FormatBool(*force), "ignoreRedundant": strconv.FormatBool(*keepRedundant), "noConfirm": strconv.FormatBool(*noConfirm)}, "upgrade", "entities")

				util.PrintResults(results, err)
			}

			fmt.Fprint(os.Stdout, "\n", "Upgrading values", "\n")

			results, err = store.Handle(map[string]interface{}{"force": strconv.FormatBool(*force)}, "pre_upgrade", "values")

			if !*noConfirm {
				util.PrintResults(results, err)
			}

			if xtracfg.HasStatus(results, xtracfg.Confirmation) {
				results, err = store.Handle(map[string]interface{}{"backup": strconv.FormatBool(*backup), "force": strconv.FormatBool(*force), "noConfirm": strconv.FormatBool(*noConfirm)}, "upgrade", "values")

				util.PrintResults(results, err)
			}

			fmt.Fprint(os.Stdout, "\n", "Upgrading layout", "\n")

			results, err = store.Handle(map[string]interface{}{"ignoreRedundant": strconv.FormatBool(*keepRedundant)}, "pre_upgrade", "layout")

			if !*noConfirm {
				util.PrintResults(results, err)
			}

			if xtracfg.HasStatus(results, xtracfg.Confirmation) {
				results, err = store.Handle(map[string]interface{}{"backup": strconv.FormatBool(*backup), "ignoreRedundant": strconv.FormatBool(*keepRedundant), "noConfirm": strconv.FormatBool(*noConfirm)}, "upgrade", "layout")

				util.PrintResults(results, err)
			}

			fmt.Fprint(os.Stdout, "\n")
		},
	}

	backup = upgrade.PersistentFlags().BoolP("backup", "b", false, "backup files before upgrading")
	force = upgrade.PersistentFlags().BoolP("force", "f", false, "upgrade files even if there are no detected issues; useful to harmonize yaml details like quoting and property order")
	keepRedundant = upgrade.PersistentFlags().BoolP("ignore-redundant", "r", false, "keep reduntant settings instead of deleting them")
	noConfirm = upgrade.PersistentFlags().BoolP("yes", "y", false, "do not ask for confirmation")

	upgrade.PersistentFlags().SortFlags = false
	upgrade.Flags().SortFlags = false

	upgradeCfg := &cobra.Command{
		Use:   "cfg",
		Short: "Upgrade cfg.yml in the store source",
		Long: `Upgrades cfg.yml with deprecated settings.
No changes are made without confirmation (unless --yes is set).`,
		Args: cobra.NoArgs,
		Run: func(cmd *cobra.Command, args []string) {
			if *debug {
				fmt.Fprint(os.Stdout, "Upgrading cfg.yml in the store source: ", store.Label(), "\n")
			}

			results, err := store.Handle(map[string]interface{}{"ignoreRedundant": strconv.FormatBool(*keepRedundant)}, "pre_upgrade", "cfg")

			if !*noConfirm {
				util.PrintResults(results, err)
			}

			if xtracfg.HasStatus(results, xtracfg.Confirmation) {
				results, err = store.Handle(map[string]interface{}{"backup": strconv.FormatBool(*backup), "ignoreRedundant": strconv.FormatBool(*keepRedundant), "noConfirm": strconv.FormatBool(*noConfirm)}, "upgrade", "cfg")

				util.PrintResults(results, err)
			}

			fmt.Fprint(os.Stdout, "\n")
		},
	}

	upgradeEntities := &cobra.Command{
		Use:   "entities [path]",
		Short: "Upgrade entities in the store source",
		Long: `Upgrades entity configurations with deprecated, unknown and redundant settings.
To upgrade only a single entity, pass the path to the file relative to the source as argument.
No changes are made without confirmation (unless --yes is set).`,
		Example: name + " upgrade entities -v -r \n" + name + " upgrade entities -v -r store/entities/services/api.yml",
		Args: func(cmd *cobra.Command, args []string) error {
			if len(args) > 1 {
				return errors.New("only one argument expected")
			}
			//TODO
			isValidPath := true
			if isValidPath {
				return nil
			}
			return fmt.Errorf("invalid entity path specified: %s", args[0])
		},
		Run: func(cmd *cobra.Command, args []string) {
			if *debug {
				fmt.Fprint(os.Stdout, "Upgrading entities for store source: ", store.Label(), "\n")
			}
			path := ""
			if len(args) > 0 {
				path = args[0]
			}

			results, err := store.Handle(map[string]interface{}{"force": strconv.FormatBool(*force), "ignoreRedundant": strconv.FormatBool(*keepRedundant), "path": path}, "pre_upgrade", "entities")

			if !*noConfirm {
				util.PrintResults(results, err)
			}

			if xtracfg.HasStatus(results, xtracfg.Confirmation) {
				results, err = store.Handle(map[string]interface{}{"backup": strconv.FormatBool(*backup), "force": strconv.FormatBool(*force), "ignoreRedundant": strconv.FormatBool(*keepRedundant), "noConfirm": strconv.FormatBool(*noConfirm), "path": path}, "upgrade", "entities")

				util.PrintResults(results, err)
			}

			fmt.Fprint(os.Stdout, "\n")
		},
	}

	upgradeValues := &cobra.Command{
		Use:   "values [path]",
		Short: "Upgrade values in the store source",
		Long: `Upgrades value configurations like stored queries with deprecated settings.
To upgrade only a single value, pass the path to the file relative to the source as argument.
No changes are made without confirmation (unless --yes is set).`,
		Example: name + " upgrade values -v \n" + name + " upgrade values -v store/values/queries/api/query.json",
		Args: func(cmd *cobra.Command, args []string) error {
			if len(args) > 1 {
				return errors.New("only one argument expected")
			}
			return nil
		},
		Run: func(cmd *cobra.Command, args []string) {
			if *debug {
				fmt.Fprint(os.Stdout, "Upgrading values in the store source: ", store.Label(), "\n")
			}
			path := ""
			if len(args) > 0 {
				path = args[0]
			}

			results, err := store.Handle(map[string]interface{}{"force": strconv.FormatBool(*force), "path": path}, "pre_upgrade", "values")

			if !*noConfirm {
				util.PrintResults(results, err)
			}

			if xtracfg.HasStatus(results, xtracfg.Confirmation) {
				results, err = store.Handle(map[string]interface{}{"backup": strconv.FormatBool(*backup), "force": strconv.FormatBool(*force), "noConfirm": strconv.FormatBool(*noConfirm), "path": path}, "upgrade", "values")

				util.PrintResults(results, err)
			}

			fmt.Fprint(os.Stdout, "\n")
		},
	}

	upgradeLayout := &cobra.Command{
		Use:   "layout",
		Short: "Upgrade layout of the store source",
		Long: `Upgrades a deprecated directory layout.
No changes are made without confirmation (unless --yes is set).`,
		Args: cobra.NoArgs,
		Run: func(cmd *cobra.Command, args []string) {
			if *debug {
				fmt.Fprint(os.Stdout, "Upgrading layout of the store source: ", store.Label(), "\n")
			}

			results, err := store.Handle(map[string]interface{}{"ignoreRedundant": strconv.FormatBool(*keepRedundant)}, "pre_upgrade", "layout")

			if !*noConfirm {
				util.PrintResults(results, err)
			}

			if xtracfg.HasStatus(results, xtracfg.Confirmation) {
				results, err = store.Handle(map[string]interface{}{"backup": strconv.FormatBool(*backup), "ignoreRedundant": strconv.FormatBool(*keepRedundant), "noConfirm": strconv.FormatBool(*noConfirm)}, "upgrade", "layout")

				util.PrintResults(results, err)
			}

			fmt.Fprint(os.Stdout, "\n")
		},
	}

	upgrade.AddCommand(upgradeCfg)
	upgrade.AddCommand(upgradeEntities)
	upgrade.AddCommand(upgradeValues)
	upgrade.AddCommand(upgradeLayout)

	return upgrade
}
