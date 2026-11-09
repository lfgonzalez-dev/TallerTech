package com.tallertech.app.data.remote.api;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;
import com.tallertech.app.data.remote.dto.auth.*;
import com.tallertech.app.data.remote.dto.orders.*;
import com.tallertech.app.data.remote.dto.equipment.*;
import com.tallertech.app.data.remote.dto.repairs.*;
import com.tallertech.app.data.remote.dto.diagnoses.*;
import com.tallertech.app.data.remote.dto.clients.*;
public interface TallerTechApi {
    @GET("api/users") Call<List<UserDto>> getUsers();
    @GET("api/users/{id}") Call<UserDto> getUser(@Path("id") String id);
    @POST("api/users") Call<UserDto> createUser(@Body UserRequest request);
    @PUT("api/users/{id}") Call<UserDto> updateUser(@Path("id") String id, @Body UserRequest request);
    @GET("api/equipment/client/{id}") Call<List<EquipmentResponse>> equipmentByClient(@Path("id") String id);
    @GET("api/orders/client/{id}") Call<List<ServiceOrderResponse>> ordersByClient(@Path("id") String id);
    @GET("api/orders/equipment/{id}") Call<List<ServiceOrderResponse>> ordersByEquipment(@Path("id") String id);
    @GET("api/diagnoses/order/{id}") Call<DiagnosisResponse> diagnosisByOrder(@Path("id") String id);
    @GET("api/repairs/order/{id}") Call<RepairResponse> repairByOrder(@Path("id") String id);
    @POST("api/auth/login") Call<LoginResponse> login(@Body LoginRequest request);
    @GET("api/auth/me") Call<UserDto> me();
    @GET("api/orders") Call<List<ServiceOrderResponse>> getServiceOrderList();
    @GET("api/orders/{id}") Call<ServiceOrderResponse> getServiceOrder(@Path("id") String id);
    @POST("api/orders") Call<ServiceOrderResponse> createServiceOrder(@Body ServiceOrderRequest request);
    @PUT("api/orders/{id}") Call<ServiceOrderResponse> updateServiceOrder(@Path("id") String id, @Body ServiceOrderRequest request);

    @GET("api/equipment") Call<List<EquipmentResponse>> getEquipmentList();
    @GET("api/equipment/{id}") Call<EquipmentResponse> getEquipment(@Path("id") String id);
    @POST("api/equipment") Call<EquipmentResponse> createEquipment(@Body EquipmentRequest request);
    @PUT("api/equipment/{id}") Call<EquipmentResponse> updateEquipment(@Path("id") String id, @Body EquipmentRequest request);

    @GET("api/repairs") Call<List<RepairResponse>> getRepairList();
    @GET("api/repairs/{id}") Call<RepairResponse> getRepair(@Path("id") String id);
    @POST("api/repairs") Call<RepairResponse> createRepair(@Body RepairRequest request);
    @PUT("api/repairs/{id}") Call<RepairResponse> updateRepair(@Path("id") String id, @Body RepairRequest request);

    @GET("api/diagnoses") Call<List<DiagnosisResponse>> getDiagnosisList();
    @GET("api/diagnoses/{id}") Call<DiagnosisResponse> getDiagnosis(@Path("id") String id);
    @POST("api/diagnoses") Call<DiagnosisResponse> createDiagnosis(@Body DiagnosisRequest request);
    @PUT("api/diagnoses/{id}") Call<DiagnosisResponse> updateDiagnosis(@Path("id") String id, @Body DiagnosisRequest request);

    @GET("api/clients") Call<List<ClientResponse>> getClientList();
    @GET("api/clients/{id}") Call<ClientResponse> getClient(@Path("id") String id);
    @POST("api/clients") Call<ClientResponse> createClient(@Body ClientRequest request);
    @PUT("api/clients/{id}") Call<ClientResponse> updateClient(@Path("id") String id, @Body ClientRequest request);
}
