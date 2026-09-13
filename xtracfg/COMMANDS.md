# Commands

## xtracfg

xtracfg provides tools to manage configurations for xtraplatform applications like [ldproxy](https://github.com/interactive-instruments/ldproxy) and XtraServer Web API.

#### Options

```
  -s, --src string      store source (default "./")
  -d, --driver string   store source driver; currently the only option is FS (default "FS")
      --help            show help
  -v, --verbose         verbose output
```

#### See Also

- [xtracfg check](#xtracfg-check) - Check the store source
- [xtracfg info](#xtracfg-info) - Print info about the store source
- [xtracfg upgrade](#xtracfg-upgrade) - Upgrade the store source

## xtracfg check

Check the store source

Check the store source
Executes all subcommands in order, see the subcommand help for details.

```
xtracfg check [flags]
```

#### Options

```
  -r, --ignore-redundant   ignore redundant settings
```

#### Options inherited from parent commands

```
  -d, --driver string   store source driver; currently the only option is FS (default "FS")
      --help            show help
  -s, --src string      store source (default "./")
  -v, --verbose         verbose output
```

#### See Also

- [xtracfg](#xtracfg)
- [xtracfg check entities](#xtracfg-check-entities) - Check entities in the store source
- [xtracfg check layout](#xtracfg-check-layout) - Check layout of the store source
- [xtracfg check values](#xtracfg-check-values) - Check values in the store source

## xtracfg check entities

Check entities in the store source

Checks entity configurations for deprecated, unknown and redundant settings.
To check only a single entity, pass the path to the file relative to the source as argument.

```
xtracfg check entities [path] [flags]
```

#### Examples

```
xtracfg check entities -v -r
xtracfg check entities -v -r store/entities/services/api.yml
```

#### Options inherited from parent commands

```
  -d, --driver string      store source driver; currently the only option is FS (default "FS")
      --help               show help
  -r, --ignore-redundant   ignore redundant settings
  -s, --src string         store source (default "./")
  -v, --verbose            verbose output
```

#### See Also

- [xtracfg check](#xtracfg-check) - Check the store source

## xtracfg check layout

Check layout of the store source

Checks for a deprecated directory layout.

```
xtracfg check layout [flags]
```

#### Options inherited from parent commands

```
  -d, --driver string      store source driver; currently the only option is FS (default "FS")
      --help               show help
  -r, --ignore-redundant   ignore redundant settings
  -s, --src string         store source (default "./")
  -v, --verbose            verbose output
```

#### See Also

- [xtracfg check](#xtracfg-check) - Check the store source

## xtracfg check values

Check values in the store source

Checks value configurations like stored queries for deprecated settings.
To check only a single value, pass the path to the file relative to the source as argument.

```
xtracfg check values [path] [flags]
```

#### Examples

```
xtracfg check values -v
xtracfg check values -v store/values/queries/api/query.json
```

#### Options inherited from parent commands

```
  -d, --driver string      store source driver; currently the only option is FS (default "FS")
      --help               show help
  -r, --ignore-redundant   ignore redundant settings
  -s, --src string         store source (default "./")
  -v, --verbose            verbose output
```

#### See Also

- [xtracfg check](#xtracfg-check) - Check the store source

## xtracfg info

Print info about the store source

```
xtracfg info [flags]
```

#### Options inherited from parent commands

```
  -d, --driver string   store source driver; currently the only option is FS (default "FS")
      --help            show help
  -s, --src string      store source (default "./")
  -v, --verbose         verbose output
```

#### See Also

- [xtracfg](#xtracfg)

## xtracfg upgrade

Upgrade the store source

Upgrade the store source
Executes all subcommands in order, see the subcommand help for details.
No changes are made without confirmation (unless --yes is set).

```
xtracfg upgrade [flags]
```

#### Options

```
  -b, --backup             backup files before upgrading
  -f, --force              upgrade files even if there are no detected issues; useful to harmonize yaml details like quoting and property order
  -r, --ignore-redundant   keep reduntant settings instead of deleting them
  -y, --yes                do not ask for confirmation
```

#### Options inherited from parent commands

```
  -d, --driver string   store source driver; currently the only option is FS (default "FS")
      --help            show help
  -s, --src string      store source (default "./")
  -v, --verbose         verbose output
```

#### See Also

- [xtracfg](#xtracfg)
- [xtracfg upgrade entities](#xtracfg-upgrade-entities) - Upgrade entities in the store source
- [xtracfg upgrade layout](#xtracfg-upgrade-layout) - Upgrade layout of the store source
- [xtracfg upgrade values](#xtracfg-upgrade-values) - Upgrade values in the store source

## xtracfg upgrade entities

Upgrade entities in the store source

Upgrades entity configurations with deprecated, unknown and redundant settings.
To upgrade only a single entity, pass the path to the file relative to the source as argument.
No changes are made without confirmation (unless --yes is set).

```
xtracfg upgrade entities [path] [flags]
```

#### Examples

```
xtracfg upgrade entities -v -r
xtracfg upgrade entities -v -r store/entities/services/api.yml
```

#### Options inherited from parent commands

```
  -b, --backup             backup files before upgrading
  -d, --driver string      store source driver; currently the only option is FS (default "FS")
  -f, --force              upgrade files even if there are no detected issues; useful to harmonize yaml details like quoting and property order
      --help               show help
  -r, --ignore-redundant   keep reduntant settings instead of deleting them
  -s, --src string         store source (default "./")
  -v, --verbose            verbose output
  -y, --yes                do not ask for confirmation
```

#### See Also

- [xtracfg upgrade](#xtracfg-upgrade) - Upgrade the store source

## xtracfg upgrade layout

Upgrade layout of the store source

Upgrades a deprecated directory layout.
No changes are made without confirmation (unless --yes is set).

```
xtracfg upgrade layout [flags]
```

#### Options inherited from parent commands

```
  -b, --backup             backup files before upgrading
  -d, --driver string      store source driver; currently the only option is FS (default "FS")
  -f, --force              upgrade files even if there are no detected issues; useful to harmonize yaml details like quoting and property order
      --help               show help
  -r, --ignore-redundant   keep reduntant settings instead of deleting them
  -s, --src string         store source (default "./")
  -v, --verbose            verbose output
  -y, --yes                do not ask for confirmation
```

#### See Also

- [xtracfg upgrade](#xtracfg-upgrade) - Upgrade the store source

## xtracfg upgrade values

Upgrade values in the store source

Upgrades value configurations like stored queries with deprecated settings.
To upgrade only a single value, pass the path to the file relative to the source as argument.
No changes are made without confirmation (unless --yes is set).

```
xtracfg upgrade values [path] [flags]
```

#### Examples

```
xtracfg upgrade values -v
xtracfg upgrade values -v store/values/queries/api/query.json
```

#### Options inherited from parent commands

```
  -b, --backup             backup files before upgrading
  -d, --driver string      store source driver; currently the only option is FS (default "FS")
  -f, --force              upgrade files even if there are no detected issues; useful to harmonize yaml details like quoting and property order
      --help               show help
  -r, --ignore-redundant   keep reduntant settings instead of deleting them
  -s, --src string         store source (default "./")
  -v, --verbose            verbose output
  -y, --yes                do not ask for confirmation
```

#### See Also

- [xtracfg upgrade](#xtracfg-upgrade) - Upgrade the store source
