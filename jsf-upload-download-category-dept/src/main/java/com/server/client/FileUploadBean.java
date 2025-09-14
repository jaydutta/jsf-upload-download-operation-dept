package com.server.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.Part;

@ManagedBean(name = "fileUploadBean")
@ViewScoped
public class FileUploadBean implements Serializable {

    private static final long serialVersionUID = 18888678L;

    // Configurable upload directory - Change this path as needed
    private static final String UPLOAD_DIRECTORY = "D:\\Example_Project_Doc\\uploads";
    // Configurable default download directory - Change this path as needed
    private static final String DEFAULT_DOWNLOAD_DIRECTORY = "D:\\Example_Project_Doc\\download";

    private Part filePath;
    private String selectedCategory;
    private String selectedDept;
    private String selectedTarget;
    private String ipAddress;

    // Download filters
    private String downloadCategoryFilter;
    
    // Save As fields - Fixed property names to match XHTML
    private String saveAsFileName;
    private String downloadLocationPath; // Changed from saveAsLocation
    private String selectedFileName;
    private UploadedItem selectedDownloadFile;

    private List<String> categories;
    private List<String> suggestedCategories = new ArrayList<>();
    private List<String> downloadSuggestedCategories = new ArrayList<>();
    private Map<String, String> categoryDeptMap;

    private List<UploadedItem> uploadedFiles = new ArrayList<>();
    private List<UploadedItem> filteredDownloadFiles = new ArrayList<>();

    public FileUploadBean() {
        initializeCategories();
        initializeCategoryDeptMapping();
        resetSuggestions();
        initializeDirectories();
        // Set default download location
        downloadLocationPath = DEFAULT_DOWNLOAD_DIRECTORY;
    }

    private void initializeDirectories() {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + UPLOAD_DIRECTORY);
            }
            
            // Create default download directory if it doesn't exist
            Path downloadPath = Paths.get(DEFAULT_DOWNLOAD_DIRECTORY);
            if (!Files.exists(downloadPath)) {
                Files.createDirectories(downloadPath);
                System.out.println("Created download directory: " + DEFAULT_DOWNLOAD_DIRECTORY);
            }
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeCategories() {
        categories = new ArrayList<>();
        categories.add("Payroll");
        categories.add("Recruitment");
        categories.add("Networking");
        categories.add("Digital Marketing");
        categories.add("Customer Support");
        categories.add("Power Management");
        categories.add("System Configuration");
        categories.add("Network Monitoring");
    }

    private void initializeCategoryDeptMapping() {
        categoryDeptMap = new HashMap<>();
      
        categoryDeptMap.put("Payroll", "Finance");
        categoryDeptMap.put("Recruitment", "HR");
        categoryDeptMap.put("Networking", "IT");
        categoryDeptMap.put("Digital Marketing", "Marketing");
        categoryDeptMap.put("Customer Support", "Operations");
        categoryDeptMap.put("Power Management", "Infrastructure");
        categoryDeptMap.put("System Configuration", "IT");
        categoryDeptMap.put("Network Monitoring", "Operations");
    }
    
    private void resetSuggestions() {
        suggestedCategories.clear();
        downloadSuggestedCategories.clear();
        updateFilteredDownloadFiles();
    }

    // Optimized suggest categories on typing (Upload)
    public void suggestCategories() {
        String query = (selectedCategory != null) ? selectedCategory.trim().toLowerCase() : "";
        suggestedCategories.clear();
        
        if (!query.isEmpty()) {
            suggestedCategories = categories.stream()
                .filter(category -> category.toLowerCase().contains(query))
                .collect(Collectors.toList());
        }
    }
    
    // Optimized suggest categories for download filter
    public void suggestDownloadCategories() {
        String query = (downloadCategoryFilter != null) ? downloadCategoryFilter.trim().toLowerCase() : "";
        downloadSuggestedCategories.clear();
        
        if (!query.isEmpty()) {
            downloadSuggestedCategories = categories.stream()
                .filter(category -> category.toLowerCase().contains(query))
                .collect(Collectors.toList());
        }
        
        updateFilteredDownloadFiles();
    }

    // Populate DeptName when Category is chosen (Upload)
    public void populateDeptFromCategory() {
        selectedDept = (selectedCategory != null && categoryDeptMap.containsKey(selectedCategory)) 
            ? categoryDeptMap.get(selectedCategory) 
            : null;
        
        // Clear suggestions after selection
        suggestedCategories.clear();
    }
    
    // Populate DeptName when Category is chosen for download
    public void populateDownloadDeptFromCategory() {
        ipAddress = (downloadCategoryFilter != null && categoryDeptMap.containsKey(downloadCategoryFilter)) 
            ? categoryDeptMap.get(downloadCategoryFilter) 
            : null;
        
        // Clear suggestions after selection
        downloadSuggestedCategories.clear();
        updateFilteredDownloadFiles();
    }

    // Enhanced upload file method with proper error handling
    public void upload() {
        try {
            if (!validateUploadFields()) {
                addErrorMessage("Upload validation failed: Please fill all required fields");
                System.out.println("Upload validation failed");
                return;
            }
            
            System.out.println("filePath: " + filePath);
            String fileName = getFileName(filePath);
            if (fileName == null || fileName.trim().isEmpty()) {
                addErrorMessage("Invalid filename");
                System.out.println("Invalid filename");
                return;
            }

            File targetFile = saveUploadedFile(fileName);
            if (targetFile != null && targetFile.exists()) {
                addUploadedFile(fileName, targetFile.getAbsolutePath());
                resetUploadForm();
                addSuccessMessage("File uploaded successfully: " + fileName);
                System.out.println("File uploaded successfully: " + fileName);
            } else {
                addErrorMessage("Failed to save uploaded file");
                System.out.println("Failed to save uploaded file");
            }
        } catch (IOException ex) {
            addErrorMessage("Error uploading file: " + ex.getMessage());
            System.err.println("Error uploading file: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            addErrorMessage("Unexpected error during upload: " + ex.getMessage());
            System.err.println("Unexpected error during upload: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean validateUploadFields() {
        boolean isValid = filePath != null 
            && selectedCategory != null && !selectedCategory.trim().isEmpty()
            && selectedDept != null && !selectedDept.trim().isEmpty();
        
        if (!isValid) {
            System.out.println("Validation failed - filePath: " + filePath + 
                             ", selectedCategory: " + selectedCategory + 
                             ", selectedDept: " + selectedDept);
        }
        
        return isValid;
    }

    private File saveUploadedFile(String fileName) throws IOException {
        // Use configured upload directory
        File targetFile = new File(UPLOAD_DIRECTORY, fileName);
        
        // Create parent directories if they don't exist
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create upload directory: " + parentDir.getAbsolutePath());
        }
        
        try (InputStream in = filePath.getInputStream();
             FileOutputStream out = new FileOutputStream(targetFile)) {
            
            byte[] buffer = new byte[8192]; // Increased buffer size
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush(); // Ensure all data is written
            
            System.out.println("File saved to: " + targetFile.getAbsolutePath());
            return targetFile;
        }
    }

    private void addUploadedFile(String fileName, String fullPath) {
        UploadedItem uploadedItem = new UploadedItem(
            fileName, 
            selectedCategory, 
            selectedDept, 
            selectedTarget != null ? selectedTarget : "Configuration", 
            selectedDept,
            fullPath // Store the full path
        );
        
        uploadedFiles.add(uploadedItem);
        updateFilteredDownloadFiles();
    }

    private void resetUploadForm() {
        selectedCategory = null;
        selectedDept = null;
        selectedTarget = null;
        filePath = null;
        suggestedCategories.clear();
    }
 
    // Optimized filter update method
    public void updateFilteredDownloadFiles() {
        if (uploadedFiles == null || uploadedFiles.isEmpty()) {
            filteredDownloadFiles = new ArrayList<>();
            return;
        }
        
        String categoryFilter = (downloadCategoryFilter != null) ? downloadCategoryFilter.toLowerCase() : "";
        String ipFilter = (ipAddress != null) ? ipAddress.toLowerCase() : "";
        
        filteredDownloadFiles = uploadedFiles.stream()
            .filter(file -> {
                boolean matchesCategory = categoryFilter.isEmpty() 
                    || file.getCategory().toLowerCase().contains(categoryFilter);
                
                boolean matchesIp = ipFilter.isEmpty() 
                    || file.getIpAddress().toLowerCase().contains(ipFilter);
                
                return matchesCategory && matchesIp;
            })
            .collect(Collectors.toList());
    }
    
    // FIXED - File selection method without AJAX dependency
    public void selectFileForDownload() {
        System.out.println("=== selectFileForDownload() called ===");
        System.out.println("selectedFileName from request: '" + selectedFileName + "'");
        
        handleFileSelection();
    }
    
    // MAIN FIX - Enhanced file selection handling with automatic selectedDownloadFile population
    public void handleFileSelection() {
        System.out.println("=== handleFileSelection() called ===");
        System.out.println("Current selectedFileName: '" + selectedFileName + "'");
        System.out.println("Current saveAsFileName: '" + saveAsFileName + "'");
        
        try {
            if (selectedFileName == null || selectedFileName.trim().isEmpty()) {
                System.out.println("selectedFileName is null or empty - clearing selection");
                selectedDownloadFile = null;
                saveAsFileName = "";
                return;
            }
            
            String trimmedFileName = selectedFileName.trim();
            System.out.println("Looking for file: '" + trimmedFileName + "'");
            
            // Find the file in the uploaded files list
            UploadedItem file = uploadedFiles.stream()
                .filter(f -> f.getName().equals(trimmedFileName))
                .findFirst()
                .orElse(null);
            
            if (file != null) {
                // THIS IS THE KEY FIX - Set BOTH selectedDownloadFile AND saveAsFileName
                selectedDownloadFile = file;
                saveAsFileName = trimmedFileName;
                
                System.out.println("✓ File selection handled successfully:");
                System.out.println("  - selectedDownloadFile: " + selectedDownloadFile.getName());
                System.out.println("  - saveAsFileName: '" + saveAsFileName + "'");
                System.out.println("  - selectedDownloadFile object: " + (selectedDownloadFile != null ? "EXISTS" : "NULL"));
                
                // Add success message for user feedback
                addInfoMessage("File selected for download: " + trimmedFileName);
                
            } else {
                System.out.println("✗ Could not find file in uploadedFiles: '" + trimmedFileName + "'");
                System.out.println("Available files (" + uploadedFiles.size() + " total):");
                for (int i = 0; i < uploadedFiles.size(); i++) {
                    UploadedItem item = uploadedFiles.get(i);
                    System.out.println("  [" + i + "] '" + item.getName() + "'");
                }
                
                // Clear selection if file not found
                selectedDownloadFile = null;
                saveAsFileName = "";
                addErrorMessage("Selected file not found: " + trimmedFileName);
            }
        } catch (Exception e) {
            System.err.println("Error in handleFileSelection: " + e.getMessage());
            e.printStackTrace();
            selectedDownloadFile = null;
            saveAsFileName = "";
            addErrorMessage("Error selecting file: " + e.getMessage());
        }
        
        System.out.println("=== handleFileSelection() completed ===");
    }

    // AJAX behavior event handler
    public void handleFileSelectionAjax(AjaxBehaviorEvent event) {
        System.out.println("=== handleFileSelectionAjax() called via AJAX ===");
        handleFileSelection();
    }
    
    // Enhanced download execution with proper validation and file copying
    public void executeDownload() {
        try {
            System.out.println("=== Starting download execution ===");
            System.out.println("selectedFileName: '" + selectedFileName + "'");
            System.out.println("saveAsFileName: '" + saveAsFileName + "'");
            System.out.println("downloadLocationPath: '" + downloadLocationPath + "'");
            System.out.println("selectedDownloadFile: " + (selectedDownloadFile != null ? selectedDownloadFile.getName() : "null"));
            
            // ADDITIONAL FIX - Try to populate selectedDownloadFile if it's null but selectedFileName is set
            if (selectedDownloadFile == null && selectedFileName != null && !selectedFileName.trim().isEmpty()) {
                System.out.println("selectedDownloadFile is null, attempting to populate from selectedFileName");
                handleFileSelection();
            }
            
            if (!validateDownloadFields()) {
                addErrorMessage("Download validation failed: Please check all required fields");
                System.out.println("Download validation failed");
                return;
            }
            
            UploadedItem originalFile = findOriginalFile();
            if (originalFile != null) {
                boolean success = performDownload(originalFile);
                if (success) {
                    addSuccessMessage("File downloaded successfully to: " + downloadLocationPath + File.separator + saveAsFileName);
                    System.out.println("Download completed successfully");
                    
                    // Clear selection after successful download
                    clearDownloadSelection();
                } else {
                    addErrorMessage("Download failed - could not copy file");
                    System.out.println("Download failed - could not copy file");
                }
            } else {
                addErrorMessage("Original file not found: " + selectedFileName);
                System.out.println("Original file not found: " + selectedFileName);
            }
        } catch (Exception ex) {
            addErrorMessage("Error during download: " + ex.getMessage());
            System.err.println("Error during download: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean validateDownloadFields() {
        boolean isValid = selectedFileName != null && !selectedFileName.trim().isEmpty()
            && saveAsFileName != null && !saveAsFileName.trim().isEmpty() 
            && downloadLocationPath != null && !downloadLocationPath.trim().isEmpty()
            && selectedDownloadFile != null;
        
        if (!isValid) {
            System.out.println("Download validation details:");
            System.out.println("- selectedFileName: '" + selectedFileName + "' (valid: " + (selectedFileName != null && !selectedFileName.trim().isEmpty()) + ")");
            System.out.println("- saveAsFileName: '" + saveAsFileName + "' (valid: " + (saveAsFileName != null && !saveAsFileName.trim().isEmpty()) + ")");
            System.out.println("- downloadLocationPath: '" + downloadLocationPath + "' (valid: " + (downloadLocationPath != null && !downloadLocationPath.trim().isEmpty()) + ")");
            System.out.println("- selectedDownloadFile: " + (selectedDownloadFile != null ? "EXISTS" : "NULL"));
        }
        
        return isValid;
    }

    private UploadedItem findOriginalFile() {
        UploadedItem found = uploadedFiles.stream()
            .filter(f -> f.getName().equals(selectedFileName))
            .findFirst()
            .orElse(null);
        
        System.out.println("Looking for file: '" + selectedFileName + "'");
        System.out.println("Found file: " + (found != null ? found.getName() : "null"));
        return found;
    }

    private boolean performDownload(UploadedItem originalFile) {
        try {
            // Ensure download directory exists
            Path downloadDir = Paths.get(downloadLocationPath);
            if (!Files.exists(downloadDir)) {
                Files.createDirectories(downloadDir);
                System.out.println("Created download directory: " + downloadLocationPath);
            }
            
            // Source file path
            String sourcePath = originalFile.getFullPath();
            if (sourcePath == null || sourcePath.trim().isEmpty()) {
                // Fallback to upload directory + filename
                sourcePath = UPLOAD_DIRECTORY + File.separator + originalFile.getName();
            }
            
            Path sourceFile = Paths.get(sourcePath);
            Path targetFile = Paths.get(downloadLocationPath, saveAsFileName);
            
            System.out.println("Copying from: " + sourceFile.toAbsolutePath());
            System.out.println("Copying to: " + targetFile.toAbsolutePath());
            
            if (!Files.exists(sourceFile)) {
                System.err.println("Source file does not exist: " + sourceFile.toAbsolutePath());
                return false;
            }
            
            // Copy the file
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("File copied successfully");
            System.out.println("Source: " + sourceFile.toAbsolutePath());
            System.out.println("Target: " + targetFile.toAbsolutePath());
            
            return true;
            
        } catch (IOException e) {
            System.err.println("IOException during file copy: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error during file copy: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Clear download selection after successful download
    private void clearDownloadSelection() {
        selectedFileName = null;
        saveAsFileName = "";
        selectedDownloadFile = null;
        System.out.println("Download selection cleared");
    }
    
    // Extract filename from Part header
    private String getFileName(Part part) {
        if (part == null) return null;
        
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String token : contentDisposition.split(";")) {
                if (token.trim().startsWith("filename")) {
                    String fileName = token.substring(token.indexOf('=') + 1).trim();
                    return fileName.replace("\"", "");
                }
            }
        }
        return null;
    }
    
    // Utility methods for JSF messages
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", message));
    }
    
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }

    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }

    // Method to browse and set download location (placeholder for future implementation)
    public void browseDownloadLocation() {
        // This method can be enhanced to integrate with file system dialogs
        System.out.println("Browse download location requested");
    }

    // FIXED - Enhanced setter with proper triggering and selectedDownloadFile population
    public void setSelectedFileName(String selectedFileName) {
        System.out.println("=== setSelectedFileName called ===");
        System.out.println("Previous value: '" + this.selectedFileName + "'");
        System.out.println("New value: '" + selectedFileName + "'");
        
        this.selectedFileName = selectedFileName;
        
        // CRITICAL FIX - Automatically handle file selection when selectedFileName is set
        if (selectedFileName != null && !selectedFileName.trim().isEmpty()) {
            handleFileSelection();
        } else {
            // Clear selection if filename is null or empty
            selectedDownloadFile = null;
            saveAsFileName = "";
        }
        
        System.out.println("=== setSelectedFileName completed ===");
    }

    // Getters and Setters
    public Part getFilePath() {
        return filePath;
    }

    public void setFilePath(Part filePath) {
        this.filePath = filePath;
    }

    public String getSelectedCategory() { 
        return selectedCategory; 
    }
    
    public void setSelectedCategory(String selectedCategory) { 
        this.selectedCategory = selectedCategory; 
    }

    public String getSelectedDept() { 
        return selectedDept; 
    }
    
    public void setSelectedDept(String selectedDept) { 
        this.selectedDept = selectedDept; 
    }

    public String getSelectedTarget() { 
        return selectedTarget; 
    }
    
    public void setSelectedTarget(String selectedTarget) { 
        this.selectedTarget = selectedTarget; 
    }

    public String getIpAddress() { 
        return ipAddress; 
    }
    
    public void setIpAddress(String ipAddress) { 
        this.ipAddress = ipAddress; 
    }

    public String getDownloadCategoryFilter() {
        return downloadCategoryFilter;
    }

    public void setDownloadCategoryFilter(String downloadCategoryFilter) {
        this.downloadCategoryFilter = downloadCategoryFilter;
    }

    public String getSaveAsFileName() {
        return saveAsFileName;
    }

    public void setSaveAsFileName(String saveAsFileName) {
        this.saveAsFileName = saveAsFileName;
    }

    public String getDownloadLocationPath() {
        return downloadLocationPath;
    }

    public void setDownloadLocationPath(String downloadLocationPath) {
        this.downloadLocationPath = downloadLocationPath;
    }

    public String getSelectedFileName() {
        return selectedFileName;
    }

    public UploadedItem getSelectedDownloadFile() {
        return selectedDownloadFile;
    }

    public void setSelectedDownloadFile(UploadedItem selectedDownloadFile) {
        this.selectedDownloadFile = selectedDownloadFile;
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<String> getSuggestedCategories() { 
        return suggestedCategories; 
    }

    public List<String> getDownloadSuggestedCategories() {
        return downloadSuggestedCategories;
    }
    
    public List<UploadedItem> getUploadedFiles() { 
        return uploadedFiles; 
    }

    public List<UploadedItem> getFilteredDownloadFiles() {
        return filteredDownloadFiles;
    }

    // Enhanced DTO for uploaded items
    public static class UploadedItem {
        private String name;
        private String category;
        private String dept;
        private String targetObject;
        private String ipAddress;
        private String fullPath; // Added to store the complete file path

        public UploadedItem(String name, String category, String dept, String targetObject, String ipAddress, String fullPath) {
            this.name = name;
            this.category = category;
            this.dept = dept;
            this.targetObject = targetObject;
            this.ipAddress = ipAddress;
            this.fullPath = fullPath;
        }
        
        // Backward compatibility constructor
        public UploadedItem(String name, String category, String dept, String targetObject, String ipAddress) {
            this(name, category, dept, targetObject, ipAddress, null);
        }
        
        public String getName() { 
            return name; 
        }
        
        public String getCategory() { 
            return category; 
        }
        
        public String getDept() { 
            return dept; 
        }
        
        public String getTargetObject() { 
            return targetObject; 
        }
        
        public String getIpAddress() { 
            return ipAddress; 
        }
        
        public String getFullPath() {
            return fullPath;
        }
        
        public void setFullPath(String fullPath) {
            this.fullPath = fullPath;
        }
    }
}