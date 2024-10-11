package org.opencdmp.filetransformer.rda.service.storage;

public interface FileStorageService {
	String storeFile(byte[] data);

	byte[] readFile(String fileRef);
}
