import { purchasesDisabled } from "../_lib/http.js";

/** Donations and case opens are disabled until payment is wired. */
export async function onRequestPost() {
  return purchasesDisabled();
}

export async function onRequestGet() {
  return purchasesDisabled();
}
