package main

import "strings"

// NeedsUpdate reports whether local install should be replaced from the remote manifest.
// Empty local version always updates. Missing launcher exe forces update even if versions match.
func NeedsUpdate(localVersion, remoteVersion string, launcherExePresent bool) bool {
	remoteVersion = strings.TrimSpace(remoteVersion)
	if remoteVersion == "" {
		return false
	}
	if !launcherExePresent {
		return true
	}
	localVersion = strings.TrimSpace(localVersion)
	if localVersion == "" {
		return true
	}
	if strings.EqualFold(localVersion, remoteVersion) {
		return false
	}
	return versionNewer(remoteVersion, localVersion)
}

// ResolveZipURL builds the absolute download URL for the launcher zip.
func ResolveZipURL(launcherZip, releaseBase string) string {
	u := strings.TrimSpace(launcherZip)
	if strings.HasPrefix(u, "http://") || strings.HasPrefix(u, "https://") {
		return u
	}
	base := strings.TrimRight(strings.TrimSpace(releaseBase), "/")
	if base == "" {
		base = "https://github.com/Renfild/AquaTeche/releases/download/client-latest"
	}
	return base + "/" + strings.TrimLeft(u, "/")
}
