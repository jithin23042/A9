# Original application source

The original application source is pinned as a Git submodule at `data/code/shopizer-3.2.7` from https://github.com/shopizer-ecommerce/shopizer.git, commit `6a4a0a65a3408ee8f62597b51d1b3aac24b77dee`.

Clone the repository including the submodule:

```powershell
git clone --recurse-submodules https://github.com/jithin23042/A9.git
```

If the repository has already been cloned:

```powershell
git submodule update --init --recursive
```

Selected source excerpts used for pattern identification are also retained under `Part1/code/original_source_evidence/`. The evolved application snapshot is under `Part2/code/shopizer-3.2.7/`.
