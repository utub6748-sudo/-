# Astro — Android personal finance & life dashboard

Astro is a local-first Android application built with Kotlin + Jetpack Compose. The supplied reference image is used as the application icon.

## Included in this build

- Personal finance ledger: income/expense, arbitrary ISO-style currency code, categories/groups, automatic totals.
- Local Room database: transactions, habits, goals/tasks and categories survive app restarts.
- Automatic FX refresh worker every 6 hours while network is available, using Frankfurter/ECB data.
- Habits with daily completion and streak counter.
- Goals/tasks with target amount and optional date/time.
- Astro AI screen; the app sends a compact local-data context to a protected Supabase Edge Function.
- Email + password registration/login and email verification code through Supabase Auth REST API.
- Local PIN lock, theme selection, accent colors, nickname and avatar picker.
- Dark/light/system themes and a minimal Material 3 visual system.

## Configure Supabase

1. Create a Supabase project.
2. Enable Email provider and configure the email template to include the verification token (`{{ .Token }}`) if you want a numeric code.
3. Put your project URL and publishable/anon key into `app/build.gradle.kts` in `SUPABASE_URL` and `SUPABASE_KEY` build fields (or move them to `local.properties` before production).
4. Deploy `supabase/functions/ai-chat/index.ts` as the `ai-chat` Edge Function.
5. Set Edge Function secrets: `OPENAI_API_KEY` and optionally `OPENAI_MODEL`.
6. Set `AI_BASE_URL` to the deployed function URL, e.g. `https://<project>.supabase.co/functions/v1/ai-chat`.

## Open in Android Studio

Use a current Android Studio release with JDK 17. Sync Gradle, set the Supabase values, then run the `app` configuration on an Android 8.0+ device/emulator.

The project is intentionally local-first: the Room database is the primary source of truth on the phone. Supabase is used for account authentication and the AI gateway; a full encrypted cloud backup/synchronization layer is left as the next production milestone rather than silently pretending it is implemented.

## Important production hardening

Before publishing: move configuration to `local.properties`/CI secrets, use Android Keystore-backed encrypted storage for session material, add per-account IDs to all local records if multiple server accounts must coexist on one device, add encrypted cloud backup/sync with Row Level Security, add automated tests, and review the financial-data threat model.

## GitHub Actions — build APK without Android Studio

1. Create a GitHub repository and upload the contents of this `Astro` folder.
2. Push the project to the `main` branch (or `master`). The workflow will start automatically.
3. Open **Actions → Build APK** in GitHub.
4. Open the completed workflow run and scroll to **Artifacts**.
5. Download **Astro-debug-apk** and extract `app-debug.apk`.
6. Transfer `app-debug.apk` to an Android phone and install it. For a debug APK, Android may ask you to allow installation from the browser/file manager.

You can also run the build manually from **Actions → Build APK → Run workflow**.

The GitHub workflow intentionally uses a pinned Gradle 9.4.1 installation, so the repository does not need a Gradle Wrapper JAR to perform the cloud build.

### Optional Supabase/AI configuration

The project can compile without `local.properties`; the generated BuildConfig values will be empty. If you need Supabase or the AI backend at runtime, configure the required values in `local.properties` for local builds or add a suitable configuration mechanism/secrets before deployment.
