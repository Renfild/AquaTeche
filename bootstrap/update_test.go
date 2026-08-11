package main

import "testing"

func TestNeedsUpdate(t *testing.T) {
	cases := []struct {
		name     string
		local    string
		remote   string
		exeOK    bool
		want     bool
	}{
		{"fresh install", "", "2.9.38", false, true},
		{"empty local with exe", "", "2.9.38", true, true},
		{"same version", "2.9.38", "2.9.38", true, false},
		{"same version case", "2.9.38", "2.9.38", true, false},
		{"bump", "2.9.37", "2.9.38", true, true},
		{"missing exe same ver", "2.9.38", "2.9.38", false, true},
		{"whitespace", " 2.9.38\n", "2.9.38", true, false},
		{"empty remote", "2.9.38", "", true, false},
		{"downgrade still updates", "2.9.40", "2.9.38", true, true},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			got := NeedsUpdate(tc.local, tc.remote, tc.exeOK)
			if got != tc.want {
				t.Fatalf("NeedsUpdate(%q,%q,%v)=%v want %v", tc.local, tc.remote, tc.exeOK, got, tc.want)
			}
		})
	}
}

func TestResolveZipURL(t *testing.T) {
	abs := "https://github.com/Renfild/AquaTeche/releases/download/client-2.9.38/AquaTechLauncher.zip"
	if got := ResolveZipURL(abs, ""); got != abs {
		t.Fatalf("absolute url rewritten: %s", got)
	}
	got := ResolveZipURL("AquaTechLauncher.zip", "https://example.com/r/client-1")
	want := "https://example.com/r/client-1/AquaTechLauncher.zip"
	if got != want {
		t.Fatalf("got %s want %s", got, want)
	}
	got = ResolveZipURL("AquaTechLauncher.zip", "")
	if !stringsHasPrefix(got, "https://github.com/Renfild/AquaTeche/releases/download/client-latest/") {
		t.Fatalf("unexpected fallback: %s", got)
	}
}

func stringsHasPrefix(s, p string) bool {
	return len(s) >= len(p) && s[:len(p)] == p
}
