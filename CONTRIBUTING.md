# Contributing & Workflow Guidelines

Thank you for contributing to **Sentinel-IP**! To maintain code quality and tracking, please strictly follow these operational rules.

---

### Issue-First Development Workflow

1. **Create an Issue First**: Before making any bug fixes, optimizations, or feature additions, always create a GitHub Issue first explaining the problem or proposed feature.
2. **Assignee**: ALWAYS assign the issue to **`@ShashankS1011`**.
3. **Comment & Reply Threading**:
   - Check if there are existing comments on the issue.
   - If someone else has commented on the issue, reply directly to their comment thread before proceeding.
4. **Branch & Fix**:
   - Create a branch or make changes on `main` referencing the issue number (e.g. `fix/#12` or `feat/#15`).
5. **Commit, Push & Close**:
   - Commit your changes with a clear message referencing the issue (e.g., `fix: resolve rate limit parser (#12)`).
   - Push the changes to GitHub.
   - Link and close the issue upon resolving it.

---

### Semantic Versioning Norms

All Sentinel-IP updates follow **Semantic Versioning (SemVer)** (`vMAJOR.MINOR.PATCH`):

* **MAJOR (`v1.0.0` -> `v2.0.0`)**: Incompatible API changes or major architectural redesigns.
* **MINOR (`v1.0.0` -> `v1.1.0`)**: Adding new features in a backward-compatible manner.
* **PATCH (`v1.0.0` -> `v1.0.1`)**: Backward-compatible bug fixes and stability improvements.

Every update increments the app version in `app/build.gradle.kts` and creates a corresponding GitHub Release with build artifacts.
