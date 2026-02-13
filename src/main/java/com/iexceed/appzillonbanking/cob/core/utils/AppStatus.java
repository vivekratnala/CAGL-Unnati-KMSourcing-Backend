package com.iexceed.appzillonbanking.cob.core.utils;

public enum AppStatus {

    INPROGRESS("INPROGRESS", "KM Sourcing – In Progress"),
    PENDING("PENDING", "Pending with BM"),
    APPROVED("APPROVED", "Pending with RPC Maker"),
    DELETED("DELETED", "Application Deleted"),
    REJECTED("REJECTED", "Rejected"),
    PUSHBACK("PUSHBACK", "Pushed back to Sourcing From NewGen"),
    IPUSHBACK("IPUSHBACK", "Pushed back to Sourcing from Iexceed"),
    ACTIVE_STATUS("A", "Active"),
    INACTIVESTATUS("I", "Inactive"),
    PENDINGFORRPCVERIFICATION("PENDINGFORRPCVERIFICATION", "Pending with RPC checker"),
    RPCVERIFIED("RPCVERIFIED", "Credit assessment"),
    RPCPUSHBACK("RPCPUSHBACK", "RPC checker to maker"),
    CACOMPLETED("CACOMPLETED", "Pending in sanction"),
    CAPUSHBACK("CAPUSHBACK", "Pushed back from CA to Sourcing"),
    PENDINGREASSESSMENT("PENDINGREASSESSMENT", "Pending Reassessment"),
    PENDINGDEVIATION("PENDINGDEVIATION", "Pending deviation"),
    PENDINGPRESANCTION("PENDINGPRESANCTION", "Pending Pre-Sanction"),
    SANCTIONED("SANCTIONED", "DB Kit generation"),
    RESANCTION("RESANCTION", "Pending Re-sanction"),
    DBKITGENERATED("DBKITGENERATED", "Pending db kit verification"),
    RPCBANKUPDATE("RPCBANKUPDATE", "RPC Bank Update"),
    DBKITVERIFIED("DBKITVERIFIED", "DB Kit Verified"),
    DISBURSED("DISBURSED", "Loan Disbursed"),
    DBPUSHBACK("DBPUSHBACK", "Pushed back to DB kit generation"),
    PENDINGSERVICECALL("PENDINGSERVICECALL", "Pending Service Call"),
    LUC("LUC","PENDING LUC"),
    PENDINGLUCVERIFICATION("PENDINGLUCVERIFICATION", "PENDING LUC VERIFICATION"),
    LUCVERIFIED("LUCVERIFIED", "LUC VERIFIED");
	
	private final String value;
    private final String stageDescription;

	AppStatus(String value, String stageDescription) {
		this.value = value;
        this.stageDescription = stageDescription;
	}
	
	public String getValue() {
		return value;
	}
    public String getStageDescription() {
        return stageDescription;
    }

    public static String getStageDescriptionByValue(String value) {
        for (AppStatus status : AppStatus.values()) {
            if (status.getValue().equals(value)) {
                return status.getStageDescription();
            }
        }
        return null; // or throw an exception if preferred
    }
}


