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

# Include any dependencies generated for this target.
include examples/api/c/CMakeFiles/linearsearch.dir/depend.make

# Include the progress variables for this target.
include examples/api/c/CMakeFiles/linearsearch.dir/progress.make

# Include the compile flags for this target's objects.
include examples/api/c/CMakeFiles/linearsearch.dir/flags.make

examples/api/c/CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.o: examples/api/c/CMakeFiles/linearsearch.dir/flags.make
examples/api/c/CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.o: ../examples/api/c/linearsearch/linearsearch.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/daniel/boolector_build/boolector/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building C object examples/api/c/CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.o"
	cd /home/daniel/boolector_build/boolector/build/examples/api/c && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.o   -c /home/daniel/boolector_build/boolector/examples/api/c/linearsearch/linearsearch.c

examples/api/c/CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.i"
	cd /home/daniel/boolector_build/boolector/build/examples/api/c && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /home/daniel/boolector_build/boolector/examples/api/c/linearsearch/linearsearch.c > CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.i

examples/api/c/CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.s"
	cd /home/daniel/boolector_build/boolector/build/examples/api/c && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /home/daniel/boolector_build/boolector/examples/api/c/linearsearch/linearsearch.c -o CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.s

# Object files for target linearsearch
linearsearch_OBJECTS = \
"CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.o"

# External object files for target linearsearch
linearsearch_EXTERNAL_OBJECTS =

bin/examples/linearsearch: examples/api/c/CMakeFiles/linearsearch.dir/linearsearch/linearsearch.c.o
bin/examples/linearsearch: examples/api/c/CMakeFiles/linearsearch.dir/build.make
bin/examples/linearsearch: lib/libboolector.so
bin/examples/linearsearch: ../deps/install/lib/libbtor2parser.a
bin/examples/linearsearch: /usr/lib/jvm/default/jre/lib/amd64/libjawt.so
bin/examples/linearsearch: /usr/lib/jvm/default/jre/lib/amd64/server/libjvm.so
bin/examples/linearsearch: ../deps/install/lib/liblgl.a
bin/examples/linearsearch: ../deps/install/lib/libcadical.a
bin/examples/linearsearch: ../deps/install/lib/libpicosat.so
bin/examples/linearsearch: examples/api/c/CMakeFiles/linearsearch.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/daniel/boolector_build/boolector/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking C executable ../../../bin/examples/linearsearch"
	cd /home/daniel/boolector_build/boolector/build/examples/api/c && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/linearsearch.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
examples/api/c/CMakeFiles/linearsearch.dir/build: bin/examples/linearsearch

.PHONY : examples/api/c/CMakeFiles/linearsearch.dir/build

examples/api/c/CMakeFiles/linearsearch.dir/clean:
	cd /home/daniel/boolector_build/boolector/build/examples/api/c && $(CMAKE_COMMAND) -P CMakeFiles/linearsearch.dir/cmake_clean.cmake
.PHONY : examples/api/c/CMakeFiles/linearsearch.dir/clean

examples/api/c/CMakeFiles/linearsearch.dir/depend:
	cd /home/daniel/boolector_build/boolector/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/daniel/boolector_build/boolector /home/daniel/boolector_build/boolector/examples/api/c /home/daniel/boolector_build/boolector/build /home/daniel/boolector_build/boolector/build/examples/api/c /home/daniel/boolector_build/boolector/build/examples/api/c/CMakeFiles/linearsearch.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : examples/api/c/CMakeFiles/linearsearch.dir/depend

