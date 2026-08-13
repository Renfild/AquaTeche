package com.casesmod.client;

import com.casesmod.network.packets.CaseListSyncS2CPacket;

import java.util.Collections;
import java.util.List;

/** Последний известный клиенту снимок списка кейсов сервера (цены, призы, доступность). */
public class ClientCaseState {
    public static List<CaseListSyncS2CPacket.CaseSnapshot> cases = Collections.emptyList();
}
