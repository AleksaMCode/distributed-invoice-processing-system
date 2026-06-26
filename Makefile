WATCHER_DIR := watcher_service
GENERATOR_DIR := invoces_generator

ifeq ($(OS),Windows_NT)
	GRADLEW := $(WATCHER_DIR)\gradlew.bat
	PYTHON := py -3
else
	GRADLEW := $(WATCHER_DIR)/gradlew
	PYTHON := python3
endif

.PHONY: watcher-build watcher-run watcher-format watcher-test generator-install generator-run precommit-install generator-format generator-test test

watcher-build:
ifeq ($(OS),Windows_NT)
	@if exist "$(GRADLEW)" ( \
		"$(GRADLEW)" -p "$(WATCHER_DIR)" build \
	) else ( \
		gradle -p "$(WATCHER_DIR)" build \
	)
else
	@if [ -f "$(GRADLEW)" ]; then \
		"$(GRADLEW)" -p "$(WATCHER_DIR)" build; \
	else \
		gradle -p "$(WATCHER_DIR)" build; \
	fi
endif

watcher-run:
ifeq ($(OS),Windows_NT)
	@if exist "$(GRADLEW)" ( \
		"$(GRADLEW)" -p "$(WATCHER_DIR)" run \
	) else ( \
		gradle -p "$(WATCHER_DIR)" run \
	)
else
	@if [ -f "$(GRADLEW)" ]; then \
		"$(GRADLEW)" -p "$(WATCHER_DIR)" run; \
	else \
		gradle -p "$(WATCHER_DIR)" run; \
	fi
endif

watcher-format:
ifeq ($(OS),Windows_NT)
	@if exist "$(GRADLEW)" ( \
		"$(GRADLEW)" -p "$(WATCHER_DIR)" spotlessApply \
	) else ( \
		gradle -p "$(WATCHER_DIR)" spotlessApply \
	)
else
	@if [ -f "$(GRADLEW)" ]; then \
		"$(GRADLEW)" -p "$(WATCHER_DIR)" spotlessApply; \
	else \
		gradle -p "$(WATCHER_DIR)" spotlessApply; \
	fi
endif

generator-install:
	@$(PYTHON) -m pip install -r "$(GENERATOR_DIR)/requirements.txt"

generator-run:
	@$(PYTHON) "$(GENERATOR_DIR)/main.py"

generator-format:
	pre-commit run --all-files

watcher-test:
ifeq ($(OS),Windows_NT)
	@if exist "$(GRADLEW)" ( \
		"$(GRADLEW)" -p "$(WATCHER_DIR)" test \
	) else ( \
		gradle -p "$(WATCHER_DIR)" test \
	)
else
	@if [ -f "$(GRADLEW)" ]; then \
		"$(GRADLEW)" -p "$(WATCHER_DIR)" test; \
	else \
		gradle -p "$(WATCHER_DIR)" test; \
	fi
endif

generator-test:
	@$(PYTHON) -m unittest discover -s "$(GENERATOR_DIR)"

test:
	@$(MAKE) watcher-test
	@$(MAKE) generator-test
