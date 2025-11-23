# matfyz-thesis-typst

Typst template for theses at Charles University in Prague. Created for Faculty of Mathematics and Physics (MFF), forked from
[CTU Thesis Typst Template](https://github.com/MatejKafka/ctu-thesis-typst), adjusted to meet thesis requirements for MFF.

## Usage
To use the template, clone the repository locally or upload it to a new project in the Typst web app, copy `example.typ`, adjust the metadata block at the top and then start writing  your thesis below.

In the web app, everything should automatically work. For local compilation, you must set a path to the font directory used for the title page when compiling:
```sh
typst compile --font-path ./template/res/fonts ./example.typ
```

If you don't, the title page will be mostly blank, except for the MFF logo.

To recompile automatically on each save, use:
```sh
typst watch --font-path ./template/res/fonts ./example.typ
```

You can use IDE plugins, which provide language server and live preview.  
Jetbrains IDEs: [Typst Support](https://plugins.jetbrains.com/plugin/27697-typst-support)  
Neovim: [Typst Preview](https://github.com/chomosuke/typst-preview.nvim)  

## Example

To view the output of the template, see [example.pdf](./example.pdf), which is compiled from `example.typ` using Typst v0.13.1.

For a more complex example, see the thesis of the original CTU template author, 
which uses the original template: [CTU Thesis](https://typst.app/project/rlLOElGGPtW50kb2HFsT1-)