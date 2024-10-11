package org.opencdmp.filetransformer.rda.audit;


import gr.cite.tools.logging.EventId;

public class AuditableAction {

    public static final EventId FileTransformer_ExportPlan = new EventId(1000, "FileTransformer_ExportPlan");
    public static final EventId FileTransformer_ExportDescription = new EventId(1001, "FileTransformer_ExportDescription");
    public static final EventId FileTransformer_ImportFileToPlan = new EventId(1002, "FileTransformer_ImportFileToPlan");
    public static final EventId FileTransformer_ImportFileToDescription = new EventId(1003, "FileTransformer_ImportFileToDescription");
    public static final EventId FileTransformer_GetSupportedFormats = new EventId(1004, "FileTransformer_GetSupportedFormats");

    
}
