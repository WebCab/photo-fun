package com.photofun.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/photos")
public class PhotoService {

  private final String UPLOADED_FILE_PATH = getUploadDirectory();
  
  @Context UriInfo uri;
  
  PhotoDao photoDao = new PhotoDao();
  
  @GET
  @Path("/fetch")
  @Produces({ MediaType.APPLICATION_JSON})
  public List<Photo> findAll(
      @DefaultValue("0") @QueryParam("from") int from,
      @DefaultValue("10")@QueryParam("to") int to) {
    return photoDao.findN(from, to);
  }
  
  @POST
  @Path("/upload")
  @Consumes("multipart/form-data")
  public Response uploadFile(MultipartFormDataInput input) {

    String fileName = "";
    String filePath = "";
    String description = "photofun service";
    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
    List<InputPart> inputParts = uploadForm.get("uploadedFile");
    
    try{
      description = input.getFormDataPart("description", String.class, null);
    }catch(Exception e){
      description = "photofun service";
    }
    
    for (InputPart inputPart : inputParts) {
      try {
        MultivaluedMap<String, String> header = inputPart.getHeaders();
        fileName = getFileName(header);
        InputStream inputStream = inputPart.getBody(InputStream.class, null);
        byte[] bytes = IOUtils.toByteArray(inputStream);
        filePath = UPLOADED_FILE_PATH + fileName;
        writeFile(bytes, filePath);
        URI baseUri = uri.getBaseUri();
        String uriHost = baseUri.getHost();
        String uriPort = baseUri.getPort() +"";
        String photoLocator = "http://"+uriHost+":"+uriPort+"/photofun/upload/"+fileName;
        Photo photoInfo = new Photo(fileName, photoLocator);
        photoInfo.setDescription(description);
        photoDao.save(photoInfo);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return Response.status(200)
        .entity("uploadFile is called, Uploaded file name : " + filePath)
        .build();
  }
  
  @POST
  @Path("/share")
  @Consumes("application/json")
  @Produces({MediaType.APPLICATION_JSON})
  public Photo sharePhoto(Photo photoToShare){
    return photoDao.sharePhoto(photoToShare.getUrl(), photoToShare.getDescription());
  }
  
  @GET
  @Path("/find/{id}")
  @Produces({ MediaType.APPLICATION_JSON})
  public Photo findOne(@PathParam("id") String id) {
    //. return photoDao.findN(Integer.parseInt(limit));
    return photoDao.findById(Integer.parseInt(id));
  }
  
  @GET
  @Path("/last")
  @Produces({ MediaType.APPLICATION_JSON})
  public Photo findLastPhoto() {
    return photoDao.getLastPhoto();
  }
  
  @GET
  @Path("/delete/{id}")
  @Produces({ MediaType.APPLICATION_JSON})
  public Photo deleteById(@PathParam("id") String id) {
    Photo photoToDelete = photoDao.findById(Integer.parseInt(id));
    if (photoToDelete != null) {
      photoDao.delete(new Photo(Integer.parseInt(id)));
      deletePhotoReferences(photoToDelete);
    }

    return photoToDelete;
  }
  
//  @PUT
//  @Path("photo/{id}")
//  @Produces({ MediaType.APPLICATION_JSON})
//  public Photo update(@PathParam("id") String id) {
//    
//  }
  
  private void deletePhotoReferences(Photo photoToDelete) {
    try {
      String photoAbsolutePath = getUploadDirectory() + photoToDelete.getName();
      File file = new File(photoAbsolutePath);
      if (file.delete()) {
        System.out.println(file.getName() + " is deleted!");
      } else {
        System.out.println("Delete operation is failed.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * header sample { Content-Type=[image/png], Content-Disposition=[form-data;
   * name="file"; filename="filename.extension"] }
   **/
  private String getFileName(MultivaluedMap<String, String> header) {

    String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
    for (String filename : contentDisposition) {
      if ((filename.trim().startsWith("filename"))) {

        String[] name = filename.split("=");

        String finalFileName = name[1].trim().replaceAll("\"", "");
        return finalFileName;
      }
    }
    return "unknown";
  }
  private void writeFile(byte[] content, String filename) throws IOException {

    File file = new File(filename);
    if (!file.exists()) {
      file.createNewFile();
    }
    FileOutputStream fop = new FileOutputStream(file);

    fop.write(content);
    fop.flush();
    fop.close();

  }

  /**
   * creates uploaded directory if not exists
   * 
   * @return path to uploaded directory
   */
  public static String getUploadDirectory() {
    String uploadDirectory = System.getProperty("catalina.base")
        + File.separator + "webapps" + File.separator + "photofun"
        + File.separator + "upload";

    File directory = new File(String.valueOf(uploadDirectory));
    if (!directory.exists()) {
      directory.mkdir();
    }

    return uploadDirectory + File.separator;
  }
}