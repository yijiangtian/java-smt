# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.15

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/daniel/boolector_build/boolector

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/daniel/boolector_build/boolector/build

# Utility rule file for examples.

# Include the progress variables for this target.
include examples/api/c/CMakeFiles/examples.dir/progress.make

examples/api/c/CMakeFiles/examples: bin/examples/array1
examples/api/c/CMakeFiles/examples: bin/examples/array2
examples/api/c/CMakeFiles/examples: bin/examples/array3
examples/api/c/CMakeFiles/examples: bin/examples/binarysearch
examples/api/c/CMakeFiles/examples: bin/examples/minor
examples/api/c/CMakeFiles/examples: bin/examples/maxor
examples/api/c/CMakeFiles/examples: bin/examples/minand
examples/api/c/CMakeFiles/examples: bin/examples/maxand
examples/api/c/CMakeFiles/examples: bin/examples/minxor
examples/api/c/CMakeFiles/examples: bin/examples/maxxor
examples/api/c/CMakeFiles/examples: bin/examples/theorems
examples/api/c/CMakeFiles/examples: bin/examples/bubblesort
examples/api/c/CMakeFiles/examples: bin/examples/bubblesortmem
examples/api/c/CMakeFiles/examples: bin/examples/bv1
examples/api/c/CMakeFiles/examples: bin/examples/bv2
examples/api/c/CMakeFiles/examples: bin/examples/doublereversearray
examples/api/c/CMakeFiles/examples: bin/examples/ispowerof2
examples/api/c/CMakeFiles/examples: bin/examples/linearsearch
examples/api/c/CMakeFiles/examples: bin/examples/matrixmultass
examples/api/c/CMakeFiles/examples: bin/examples/matrixmultcomm
examples/api/c/CMakeFiles/examples: bin/examples/max
examples/api/c/CMakeFiles/examples: bin/examples/memcpy
examples/api/c/CMakeFiles/examples: bin/examples/nextpowerof2
examples/api/c/CMakeFiles/examples: bin/examples/selectionsort
examples/api/c/CMakeFiles/examples: bin/examples/selectionsortmem
examples/api/c/CMakeFiles/examples: bin/examples/sudoku
examples/api/c/CMakeFiles/examples: bin/examples/swapmem
examples/api/c/CMakeFiles/examples: bin/examples/exception


examples: examples/api/c/CMakeFiles/examples
examples: examples/api/c/CMakeFiles/examples.dir/build.make

.PHONY : examples

# Rule to build all files generated by this target.
examples/api/c/CMakeFiles/examples.dir/build: examples

.PHONY : examples/api/c/CMakeFiles/examples.dir/build

examples/api/c/CMakeFiles/examples.dir/clean:
	cd /home/daniel/boolector_build/boolector/build/examples/api/c && $(CMAKE_COMMAND) -P CMakeFiles/examples.dir/cmake_clean.cmake
.PHONY : examples/api/c/CMakeFiles/examples.dir/clean

examples/api/c/CMakeFiles/examples.dir/depend:
	cd /home/daniel/boolector_build/boolector/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/daniel/boolector_build/boolector /home/daniel/boolector_build/boolector/examples/api/c /home/daniel/boolector_build/boolector/build /home/daniel/boolector_build/boolector/build/examples/api/c /home/daniel/boolector_build/boolector/build/examples/api/c/CMakeFiles/examples.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : examples/api/c/CMakeFiles/examples.dir/depend

