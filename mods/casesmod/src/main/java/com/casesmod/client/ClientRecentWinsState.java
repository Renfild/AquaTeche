package com.casesmod.client;

import com.casesmod.data.RecentWinsManager;

import java.util.Collections;
import java.util.List;

/** Последний известный клиенту снимок ленты выигрышей — обновляется при рассылке с сервера. */
public class ClientRecentWinsState {
    public static List<RecentWinsManager.WinEntry> entries = Collections.emptyList();
}
