# Frogue Timer on Android phones and Fire TV: setup guide

How it works: the **Frogue Timer app** is a thin shell. It opens your normal timer page (the one on GitHub Pages) full screen. One app file installs on both:

- **On a phone** it's the normal timer, with the screen kept awake during a workout.
- **On a Fire TV** it opens straight onto the TV screen (the big code and QR), keeps the TV awake, and works with the remote.

The web app stays the only copy of the timer. A normal upload of `index.html` updates the app too. You only build a new app file when the `android/` folder changes.

Do the parts in order. Parts 1 and 2 are done once.

---

## Part 1 · Get the files onto GitHub

The easy way: merge the pull request that adds the app. Everything below is already in it.

By hand instead: in the interval-timer repo, **Add file → Upload files**, drag in these, then **Commit changes**:

| File or folder | What to do |
|---|---|
| `index.html` | replace the old one |
| `sw.js` | replace the old one (it now says `interval-timer-v10`; that is what makes installed copies pick up the update) |
| `ANDROID-GUIDE.md` | **new**, this guide |
| `android/` folder | **new**, the app's source |
| `.github/workflows/build-frogue-app.yml` | **new**, tells GitHub how to build the app |

The `.github` folder is hidden on a Mac (press Cmd+Shift+. in Finder to show it). If your browser won't take it: **Add file → Create new file**, type `.github/workflows/build-frogue-app.yml` as the name, paste in the file's contents and commit.

In a browser the timer behaves exactly as before.

---

## Part 2 · Add the signing key (5 min)

Every copy of the app is signed with one key. Android only installs an update over the top (keeping your workouts) if it's signed with the **same** key, so this key must never change.

You have two private files (in the project folder, `private/`): `FROGUE_KEYSTORE.txt` and `FROGUE_KEY_PASSWORD.txt`.

1. interval-timer repo → **Settings → Secrets and variables → Actions → New repository secret**.
2. Name `FROGUE_KEYSTORE`, value: the whole contents of `FROGUE_KEYSTORE.txt`. **Add secret**.
3. Again: name `FROGUE_KEY_PASSWORD`, value: the contents of `FROGUE_KEY_PASSWORD.txt`. **Add secret**.
4. **Keep a copy of both files somewhere safe** (a password manager or a private drive). GitHub won't show a secret again.

---

## Part 3 · Build the app (5 min)

1. Repo → **Actions** tab. If it asks, click **I understand my workflows, go ahead and enable them**.
2. **Build Frogue app** → **Run workflow** → leave the address empty → **Run workflow**.
   It finds the timer's GitHub Pages address by itself and prints it in the log ("The timer's address: …").
3. After about 4–6 minutes click the finished run → under **Artifacts** download **Frogue-app**. It's a zip; inside is `Frogue-N.apk`.

---

## Part 4 · Install it on a phone (5 min)

1. Unzip and send `Frogue-N.apk` to the phone (Google Drive, email, or USB). Open it on the phone.
2. Android asks to allow installs from that app (Drive, Gmail or Files). Allow it, go back, tap **Install**. If Play Protect says it doesn't recognise the developer: **More details → Install anyway**.
3. Open **Frogue Timer**.

**Your workouts don't come across by themselves.** The app has its own storage, separate from Chrome. To bring them over: in Chrome's timer tap **⋯ → Export all**, then in the app **⋯ → Import** and pick that file.

To control the TV from the app: **📱 Control a TV** and type the code. (Scanning the TV's QR code with the camera opens Chrome's copy, not the app.)

---

## Part 5 · Install it on the Fire TV (10 min)

**Which stick?** Settings → My Fire TV → About. Newer sticks running **Vega OS** can't install outside apps at all. Use the Silk browser with your address plus `?display` instead. Fire OS sticks, the Cube and Fire TV televisions can.

1. **Developer options:** Settings → My Fire TV → About → click the first item (your stick's name) **7 times**. Then Settings → My Fire TV → Developer options → turn on **Apps from Unknown Sources** (newer Fire OS: **Install unknown apps** → Downloader → On).
2. Install **Downloader** (orange icon, by AFTVnews) from the Fire TV app store.
3. Give the APK a web link. Easiest: repo → **Releases → Create a new release**, tag `app-N`, drag `Frogue-N.apk` in, **Publish**. Right-click the APK there → copy link.
4. In Downloader, type that link (or shorten it first with a link shortener; typing with the remote is slow) → it downloads → **Install**.
5. Open it: Home → **Apps** (the three squares) → **Frogue Timer**. Hold OK on it → **Move** to put it on the front row.

If you'd installed the earlier Fire TV-only app ("Frogue Timer" built from `firetv/`), uninstall that one; this replaces it.

**The remote:**

| Button | During a workout |
|---|---|
| OK or ⏯ Play/Pause | pause / resume (resume counts you in 3-2-1) |
| ◀ / ⏪ | back |
| ▶ / ⏩ | skip |
| Back | "Stop this workout?" (◀ ▶ pick, OK chooses) |

On the code screen, Back leaves the app. If the green **"Press OK to turn the sound on"** button shows, press OK once.

---

## Updates later

- **Changes to the timer itself** (almost all of them): upload `index.html` and `sw.js` as usual. The app picks them up next time it opens with internet.
- **Changes to the app shell** (only when you get a new `android/` folder): run **Build Frogue app** again and install the new APK over the top. Same key, so nothing is lost.

---

## If something goes wrong

| Problem | Try |
|---|---|
| The workflow says the secrets are missing | Part 2: the names must be exactly `FROGUE_KEYSTORE` and `FROGUE_KEY_PASSWORD`. |
| The workflow can't find the address | Type the full address (starting `https://`) in the box when you run it. |
| "App not installed" when updating | It was signed with a different key. Check the secrets are the same ones as before. |
| "Frogue can't reach the timer yet" | It needs internet the first time it opens, and tries again every 5 seconds by itself. |
| The phone screen still goes dark mid-workout | Setup screen → check **Keep screen awake during workouts** is on. |
| The Fire TV shows the normal setup screen | You pressed **Exit TV mode**. Press Back to close the app and open it again; it always opens in TV mode. |

---
A Bouncy Production · Made for Haz
