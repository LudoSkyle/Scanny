# fastlane builds and publishes the apps: see fastlane/Fastfile and docs/Common/04_BUILD.md
source "https://rubygems.org"

gem "fastlane", "~> 2.242"

plugins_path = File.join(File.dirname(__FILE__), "fastlane", "Pluginfile")
eval_gemfile(plugins_path) if File.exist?(plugins_path)
