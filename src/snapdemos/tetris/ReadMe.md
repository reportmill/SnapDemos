# Tetris

![](https://reportmill.com/SnapCode/Samples/Tetris/Tetris.png)

This is a simple clone of the Tetris game, written with SnapKit.

## TetrisPane

This is the main app class. It defines the UI (in TetrisPane.snp) to show the PlayView, the
main title, the next block box and other minior UI controls.

## PlayView

This class is the main game view and manages the falling block and a list of stack rows for
blocks that have already fallen.

## Block

This class represents a single block and holds its pattern.

## Pattern

This class represents the available block patterns in the game.
