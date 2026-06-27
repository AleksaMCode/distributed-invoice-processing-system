# 97 108 101 107 115 97
WATCHER_DIR := watcher_service
VALIDATOR_DIR := validator_service
GENERATOR_DIR := invoces_generator

ifeq ($(OS),Windows_NT)
	GRADLEW := $(WATCHER_DIR)\gradlew.bat
	VALIDATOR_GRADLEW := $(VALIDATOR_DIR)\gradlew.bat
	PYTHON := py -3
else
	GRADLEW := $(WATCHER_DIR)/gradlew
	VALIDATOR_GRADLEW := $(VALIDATOR_DIR)/gradlew
	PYTHON := python3
endif

.PHONY: watcher-build watcher-run watcher-format watcher-test validator-build validator-run validator-format validator-test generator-install generator-run precommit-install generator-format generator-test format test

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

validator-build:
ifeq ($(OS),Windows_NT)
	@if exist "$(VALIDATOR_GRADLEW)" ( \
		"$(VALIDATOR_GRADLEW)" -p "$(VALIDATOR_DIR)" build \
	) else ( \
		gradle -p "$(VALIDATOR_DIR)" build \
	)
else
	@if [ -f "$(VALIDATOR_GRADLEW)" ]; then \
		"$(VALIDATOR_GRADLEW)" -p "$(VALIDATOR_DIR)" build; \
	else \
		gradle -p "$(VALIDATOR_DIR)" build; \
	fi
endif

validator-run:
ifeq ($(OS),Windows_NT)
	@if exist "$(VALIDATOR_GRADLEW)" ( \
		"$(VALIDATOR_GRADLEW)" -p "$(VALIDATOR_DIR)" run \
	) else ( \
		gradle -p "$(VALIDATOR_DIR)" run \
	)
else
	@if [ -f "$(VALIDATOR_GRADLEW)" ]; then \
		"$(VALIDATOR_GRADLEW)" -p "$(VALIDATOR_DIR)" run; \
	else \
		gradle -p "$(VALIDATOR_DIR)" run; \
	fi
endif

validator-format:
ifeq ($(OS),Windows_NT)
	@if exist "$(VALIDATOR_GRADLEW)" ( \
		"$(VALIDATOR_GRADLEW)" -p "$(VALIDATOR_DIR)" spotlessApply \
	) else ( \
		gradle -p "$(VALIDATOR_DIR)" spotlessApply \
	)
else
	@if [ -f "$(VALIDATOR_GRADLEW)" ]; then \
		"$(VALIDATOR_GRADLEW)" -p "$(VALIDATOR_DIR)" spotlessApply; \
	else \
		gradle -p "$(VALIDATOR_DIR)" spotlessApply; \
	fi
endif

validator-test:
ifeq ($(OS),Windows_NT)
	@if exist "$(VALIDATOR_GRADLEW)" ( \
		"$(VALIDATOR_GRADLEW)" -p "$(VALIDATOR_DIR)" test \
	) else ( \
		gradle -p "$(VALIDATOR_DIR)" test \
	)
else
	@if [ -f "$(VALIDATOR_GRADLEW)" ]; then \
		"$(VALIDATOR_GRADLEW)" -p "$(VALIDATOR_DIR)" test; \
	else \
		gradle -p "$(VALIDATOR_DIR)" test; \
	fi
endif

generator-install:
	@$(PYTHON) -m pip install -r "$(GENERATOR_DIR)/requirements.txt"

generator-run:
	@$(PYTHON) "$(GENERATOR_DIR)/main.py"

generator-format:
	pre-commit run --all-files

generator-test:
	@$(PYTHON) -m unittest discover -s "$(GENERATOR_DIR)"

test:
	@$(MAKE) watcher-test
	@$(MAKE) validator-test
	@$(MAKE) generator-test

format:
	@$(MAKE) watcher-format
	@$(MAKE) validator-format
	@$(MAKE) generator-format