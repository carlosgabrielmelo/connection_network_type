# Contribution Guidelines

## Contribution types

### Bug Reports
 - If you find a bug, please first report it using [Github issues]. Check if there is not a open issue related.

### Bug Fix
 - If you'd like to submit a fix for a bug, please read the [How To](#how-to-contribute) for how to
   send a Pull Request.
 - Indicate on the open issue that you are working on fixing the bug and the issue will be assigned
   to you.
 - Write `Fixes #xxxx` in your PR text, where xxxx is the issue number (if there is one).
 - Include a test that isolates the bug and verifies that it was fixed.

### New Features
 - If you'd like to add a feature to the library that doesn't already exist, feel free to describe
   the feature in a new [GitHub issue].
 - Implement the code for the new feature and please read the [How To](#how-to-contribute).

### Documentation & Miscellaneous
 - If you have suggestions for improvements as always first file a [Github issue].
 - Implement the changes to the documentation, please read the [How To](#how-to-contribute).

## How To Contribute

### Requirements
For a contribution to be accepted:

- Documentation should always be updated or added.*
- Examples should always be updated or added.*
- Tests should always be updated or added.*
- Format the Dart code accordingly with `flutter format`.
- Your code should pass the analyzer checks `flutter run analyze`.
- Your code should pass all tests `flutter run test`.
- Start your PR title with a [conventional commit] type

If the contribution doesn't meet these criteria, a maintainer will discuss it with you on the issue
or PR. You can still continue to add more commits to the branch you have sent the Pull Request from
and it will be automatically reflected in the PR.

## Open an issue and fork the repository
 - If it is a bigger change or a new feature, first of all
   [file a bug or feature report][GitHub issues], so that we can discuss what direction to follow.
 - [Fork the project][fork guide] on GitHub.

### Performing changes
 - Create a new local branch from `main` (e.g. `git checkout -b [change_type]/my-new-feature`)
 - Make your changes.
 - When committing your changes, make sure that each commit message is clear
 (e.g. `git commit -m '[commit_type]: Update documentation'`).

### Open a pull request
Go to the [pull request page of connection_network_type][PRs] and in the top
of the page it will ask you if you want to open a pull request from your newly created branch.

The title of the pull request should start with a [conventional commit] type.

Examples of such types:
 - `fix:` - patches a bug and is not a new feature.
 - `feat:` - introduces a new feature.
 - `docs:` - updates or adds documentation or examples.
 - `test:` - updates or adds tests.
 - `refactor:` - refactors code but doesn't introduce any changes or additions to the public API.

If you introduce a **breaking change** the conventional commit type MUST end with an exclamation
mark (e.g. `feat!: Update the minimum version of compileSdkVersion`).

Examples of PR titles:
 - feat!: Update the minimum version of compileSdkVersion.
 - feat: Added new function.
 - docs: Add examples documentation.
 - test: Add test to new function.
 - chore: Update files for new release.
 - refactor: Refact on the native code.

[GitHub issue]: https://github.com/carlosgabrielmelo/connection_network_type/issues/new
[GitHub issues]: https://github.com/carlosgabrielmelo/connection_network_type/issues/new
[PRs]: https://github.com/carlosgabrielmelo/connection_network_type/pulls
[fork guide]: https://guides.github.com/activities/forking/#fork
[pubspec doc]: https://dart.dev/tools/pub/pubspec
[conventional commit]: https://www.conventionalcommits.org