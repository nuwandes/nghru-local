package org.southasia.ghru.api

import androidx.lifecycle.LiveData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.southasia.ghru.jobs.SyncHouseholdMemberJob
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.*
import org.southasia.ghru.vo.request.Member
import retrofit2.Call
import retrofit2.http.*


/**
 * REST API access points
 */
interface NghruService {

    @Headers(
        "Accept: application/json",
        "Content-type:application/json"
    )
    @FormUrlEncoded
    @POST("api/login")
    fun getAccessToken(
        @Field(value = "email", encoded = false) email: String, @Field(
            value = "password",
            encoded = false
        ) password: String
    ): LiveData<ApiResponse<ResourceData<LoginData>>>

    @FormUrlEncoded
    @POST("api/login")
    fun getAccessToken(@FieldMap test: Map<String, String>): LiveData<ApiResponse<AccessToken>>

    @POST("api/refresh")
    fun getRefresh(@Body refreshToken: RefreshToken): LiveData<ApiResponse<AccessToken>>


    @FormUrlEncoded
    @POST("api/login")
    fun getAccessTokenOld(@FieldMap test: Map<String, String>): LiveData<ApiResponse<ResourceData<LoginData>>>

    @POST("api/household")
    fun addHousehold(@Body household: Household): LiveData<ApiResponse<ResourceData<HouseholdData>>>

    @POST("api/v2/household")
    fun addHouseholdRequest(@Body household: HouseholdRequestMeta): LiveData<ApiResponse<ResponceData>>

    @POST("api/v2/household")
    fun addHouseholdRequestSync(@Body household: HouseholdRequestMeta): Call<ResourceData<HouseholdData>>

    @POST("api/household/{householdId}/members")
    fun addMember(@Body member: Member, @Path("householdId") householdId: String): LiveData<ApiResponse<ResourceData<MemberData>>>

    @POST("api/household/{householdId}/members")
    fun addMemberSync(@Body member: List<SyncHouseholdMemberJob.MemberDTO>, @Path("householdId") householdId: String): Call<ResourceData<List<SyncHouseholdMemberJob.MemberDTO>>>

    @POST("api/household/{householdId}/members")
    fun addMembers(@Body member: List<SyncHouseholdMemberJob.MemberDTO>, @Path("householdId") householdId: String): LiveData<ApiResponse<ResourceData<List<SyncHouseholdMemberJob.MemberDTO>>>>

    @GET("api/household/{enumeration_id}/members")
    fun getMember(@Path("enumeration_id") householdId: String, @Query("registered") registered: String): LiveData<ApiResponse<ResourceData<List<Member>>>>

    @GET("api/v2/household/{enumeration_id}")
    fun getHouseHold(@Path("enumeration_id") enumerationId: String): LiveData<ApiResponse<ResourceData<HouseholdBodyData>>>

    @GET("api/v2/household")
    fun getHouseHolds(): LiveData<ApiResponse<ResourceData<List<HouseholdRequestMetaResponce>>>>


    @GET("api/v2/household/{enumeration_id}")
    fun getHouseHoldX(@Path("enumeration_id") enumerationId: String): LiveData<ApiResponse<ResourceData<HouseholdRequestMeta>>>

    @GET("api/profile/screening/{screening_id}")
    fun getParticipant(@Path("screening_id") screeningId: String): LiveData<ApiResponse<ResourceData<Participant>>>

    @GET("api/profile/screening/{screening_id}")
    fun getParticipantRequest(@Path("screening_id") screeningId: String): LiveData<ApiResponse<ResourceData<ParticipantRequest>>>

    @GET("api/profile/screening/{screening_id}")
    fun getParticipantRequest(@Path("screening_id") screeningId: String, @Query("station") station: String): LiveData<ApiResponse<ResourceData<ParticipantRequest>>>


    @GET("api/user")
    fun getUser(): LiveData<ApiResponse<ResourceData<User>>>


    @POST("api/participants")
    fun addParticipantRequest(@Body participantRequest: ParticipantRequest): LiveData<ApiResponse<ResourceData<Participant>>>


    @POST("api/participants")
    fun addParticipantRequestSync(@Body participantRequest: ParticipantRequest): Call<ResourceData<ParticipantRequest>>


    @GET("api/assets/screening/{screening_id}")
    fun getAssets(@Path("screening_id") screeningId: String, @Query("purpose") purpose: String): LiveData<ApiResponse<ResourceData<List<Asset>>>>


    @Multipart
    @POST("api/assets")
    fun upload(
        @Part image: MultipartBody.Part, @Part("screening_id") screeningId: RequestBody, @Part("subject_id") subjectId: RequestBody, @Part(
            "subject_type"
        ) subjectType: RequestBody, @Part("purpose") purpose: RequestBody
    ): LiveData<ApiResponse<Message>>

    @Multipart
    @POST("api/assets")
    fun uploadBackground(
        @Part image: MultipartBody.Part, @Part("screening_id") screeningId: RequestBody, @Part("subject_id") subjectId: RequestBody, @Part(
            "subject_type"
        ) subjectType: RequestBody, @Part("purpose") purpose: RequestBody
    ): Call<Message>


    @POST("api/screening/{screening_id}/body-measurement")
    fun addBodyMeasurementRequest(@Path("screening_id") screeningId: String, @Body bodyMeasurementRequest: BodyMeasurementRequest): LiveData<ApiResponse<ResourceData<BodyMeasurementRequest>>>

    @POST("api/screening/{screening_id}/body-measurement")
    fun addBodyMeasurementMeta(@Path("screening_id") screeningId: String, @Body bodyMeasurementRequest: BodyMeasurementMeta): LiveData<ApiResponse<ResourceData<Message>>>


    @GET("api/screening/{screening_id}/body-measurement")
    fun getBodyMeasurementMeta(@Path("screening_id") screeningId: String): LiveData<ApiResponse<BodyMeasurementMetaResonce>>


    @POST("api/screening/{screening_id}/body-measurement")
    fun addBodyMeasurementRequestSync(@Path("screening_id") screeningId: String, @Body bodyMeasurementRequest: BodyMeasurementRequest): Call<ResourceData<Message>>

    @POST("api/screening/{screening_id}/body-measurement")
    fun addBodyMeasurementMetaSync(@Path("screening_id") screeningId: String, @Body bodyMeasurementRequest: BodyMeasurementMeta): Call<ResourceData<BodyMeasurementRequest>>


    @POST("api/screening/{screening_id}/ecg")
    fun addECGSync(@Path("screening_id") screeningId: String, @Body mECGStatus: ECGStatus): LiveData<ApiResponse<ResourceData<ECG>>>

    @POST("api/screening/{screening_id}/ecg")
    fun addECG(@Path("screening_id") screeningId: String, @Body mECGStatus: ECGStatus): Call<ResourceData<ECG>>

    @POST("api/screening/{screening_id}/fundoscopy")
    fun addFundoscopyGSync(@Path("screening_id") screeningId: String, @Body fundoscopyRequest: FundoscopyRequest?): LiveData<ApiResponse<ResourceData<ECG>>>

    @POST("api/screening/{screening_id}/fundoscopy")
    fun addFundoscopy(@Path("screening_id") screeningId: String, @Body comment: FundoscopyRequest?): Call<ResourceData<ECG>>


    @POST("api/screening/{screening_id}/sample/{sample_id}")
    fun addSampleSync(@Path("screening_id") screeningId: String, @Path("sample_id") sampleId: String,@Body comment: SampleCreateRequest?): LiveData<ApiResponse<ResourceData<SampleData>>>

    @POST("api/screening/{screening_id}/sample/{sample_id}")
    fun addSample(@Path("screening_id") screeningId: String, @Path("sample_id") sampleId: String,@Body comment: SampleCreateRequest?): Call<ResourceData<SampleData>>


    @PUT("api/sample/{sample_id}")
    fun addSampleProcessSync(@Path("sample_id") sampleId: String, @Body sampleProcess: SampleProcess): LiveData<ApiResponse<Message>>

    @PUT("api/sample/{sample_id}")
    fun addSampleProcess(@Path("sample_id") sampleId: String, @Body sampleProcess: SampleProcess): Call<ResourceData<Message>>


    @PUT("api/sample/{sample_id}")
    fun addSampleStorageSync(@Path("sample_id") sampleId: String, @Body sampleStorageRequest: SampleStorageRequest): LiveData<ApiResponse<ResourceData<SampleData>>>

    @PUT("api/sample/{sample_id}")
    fun addSampleStoyage(@Path("sample_id") sampleId: String, @Body sampleStorageRequest: SampleStorageRequest): Call<ResourceData<SampleData>>


    @POST("api/screening/{screening_id}/spirometry")
    fun addSpirometrySync(@Path("screening_id") screeningId: String, @Body spirometryData: SpirometryRequest): LiveData<ApiResponse<ResourceData<CommonResponce>>>


    @POST("api/screening/{screening_id}/spirometry")
    fun addSpirometry(@Path("screening_id") screeningId: String, @Body spirometryData: SpirometryRequest): Call<ResourceData<CommonResponce>>


    @POST("/api/v2/participants")
    fun addParticipantMetaSync(@Body participantMeta: ParticipantMeta): LiveData<ApiResponse<ResourceData<CommonResponce>>>

    @GET("/api/v2/participants/screening/{screening_id}")
    fun getParticipantRequestMeta(@Path("screening_id") screeningId: String): LiveData<ApiResponse<ResourceData<ParticipantMeta>>>

    @GET("/api/v2/participants")
    fun getParticipantRequestMetas(): LiveData<ApiResponse<ResourceData<List<ParticipantMeta>>>>

    @POST("/api/v2/participants")
    fun addParticipantMeta(@Body participantMeta: ParticipantMeta): Call<ResourceData<CommonResponce>>

    @POST("/api/questionnaire/result")
    fun addSurveySync(@Body survey: QuestionMeta): LiveData<ApiResponse<ResourceData<CommonResponce>>>

    @POST("/api/screening/{screening_id}/report")
    fun addSurveyCompleteSync(@Path("screening_id") screeningId: String, @Body reportRequestMeta: ReportRequestMeta): LiveData<ApiResponse<ResourceData<CommonResponce>>>


    @POST("/api/questionnaire/result")
    fun addSurvey(@Body survey: QuestionMeta): Call<ResourceData<CommonResponce>>

    @GET("/api/sample")
    fun getSamplePending(@Query("status") status: String): LiveData<ApiResponse<ResourceData<List<SampleRequest>>>>


    @GET("/api/sample")
    fun getSamples(): LiveData<ApiResponse<ResourceData<List<SampleRequest>>>>


    @GET("/api/sample/{sample_id}")
    fun getSampleBySampleId(@Path("sample_id") sampleId: String): LiveData<ApiResponse<ResourceData<SampleRequest>>>

    @GET("/api/storage/{storage_d}")
    fun getSampleByStorageId(@Path("storage_d") storageId: String): LiveData<ApiResponse<ResourceData<SampleRequest>>>


    @POST("/api/storage/{storage_id}")
    fun addStorageSync(@Path("storage_id") storageId: String, @Body storageDto: StorageDto): LiveData<ApiResponse<Message>>


    @POST("/api/storage/{storage_id}")
    fun addStorage(@Path("storage_id") storageId: String, @Body storageDto: StorageDto): Call<ResourceData<Message>>


    @PUT("api/screening/{screening_id}/station/cancel")
    fun addCancelRequest(@Path("screening_id") screeningId: String, @Body cancelRequest: CancelRequest): LiveData<ApiResponse<ResourceData<MessageCancel>>>

    @PUT("api/sample/{sample_id}/station/cancel")
    fun addCancelSampleRequest(@Path("sample_id") screeningId: String, @Body cancelRequest: CancelRequest): LiveData<ApiResponse<ResourceData<Message>>>

    @PUT("api/storage/{storage_id}/station/cancel")
    fun addCancelStorageRequest(@Path("storage_id") screeningId: String, @Body cancelRequest: CancelRequest): LiveData<ApiResponse<ResourceData<Message>>>


    @PUT("api/screening/{screening_id}/station/cancel")
    fun addCancelRequestSync(@Path("screening_id") screeningId: String, @Body cancelRequest: CancelRequest): Call<ResourceData<Message>>

    @PUT("api/screening/{screening_id}/station/cancel")
    fun addCancelAxivityRequestSync(@Path("screening_id") screeningId: String, @Body cancelRequest: CancelRequest): Call<ResourceData<MessageCancel>>

    @PUT("api/sample/{sample_id}/station/cancel")
    fun addCancelSampleRequestSync(@Path("sample_id") screeningId: String, @Body cancelRequest: CancelRequest): Call<ResourceData<Message>>

    @PUT("api/storage/{storage_id}/station/cancel")
    fun addCancelStorageRequestSync(@Path("storage_id") screeningId: String, @Body cancelRequest: CancelRequest): Call<ResourceData<Message>>

    @GET("/api/devices?status=active")
    fun getStationDevices(): LiveData<ApiResponse<ResourceData<List<StationDeviceData>>>>

//    @POST("api/assets")
//    fun upload( @Part image: MultipartBody.Part, @Field(value = "screening_id", encoded = false) screeningId: String, @Field(value = "subject_id", encoded = false) subjectId: String, @Field(value = "subject_type", encoded = false) subjectType: String, @Field(value = "purpose", encoded = false) purpose: RequestBody): LiveData<ApiResponse<String>>
//
//    @Multipart
//    @POST("api/assets")
//    fun upload( @FieldMap test: Map<String, String>, @Part image: MultipartBody.Part): LiveData<ApiResponse<String>>
//
//    @Multipart
//    @POST("api/assets")
//    fun uploadTest( @PartMap params: Map<String, RequestBody>): LiveData<ApiResponse<String>>

    @POST("api/screening/{screening_id}/blood-pressure")
    fun addBloodPresureRequest(@Path("screening_id") screeningId: String, @Body bloodPressureMetaRequest: BloodPressureMetaRequest): LiveData<ApiResponse<ResourceData<BloodPressureMetaRequest>>>

    @POST("api/screening/{screening_id}/blood-pressure")
    fun addBloodPresureRequestSync(@Path("screening_id") screeningId: String, @Body bloodPressureMetaRequest: BloodPressureMetaRequest): Call<ResourceData<BloodPressureMetaRequest>>


    @POST("api/screening/{screening_id}/axivity")
    fun addAxivity(@Path("screening_id") screeningId: String, @Body axivity: Axivity): LiveData<ApiResponse<ResourceData<Message>>>

    @POST("api/screening/{screening_id}/axivity")
    fun addAxivitySync(@Path("screening_id") screeningId: String, @Body axivity: Axivity): Call<ResourceData<Message>>

    @GET("api/questionnaire/{language}")
    fun getQuestionnaire(@Path("language") language: String): LiveData<ApiResponse<ResourceData<Questionnaire>>>

    @GET("api/questionnaire")
    fun getQuestionnaire(): LiveData<ApiResponse<ResourceData<List<Questionnaire>>>>

    @POST("/api/screening/{screening_id}/intake")
    fun postIntake(@Body body : IntakeRequestNew , @Path("screening_id") screening_id: String): LiveData<ApiResponse<ResourceData<IntakeResponse>>>

    @PUT("/api/screening/{screening_id}/intake")
    fun updateIntake(@Body body : IntakeRequestNew , @Path("screening_id") screening_id: String): LiveData<ApiResponse<ResourceData<IntakeResponse>>>

//    @GET("/api/devices?measurement=hemoglobin")
//    fun getHemoglobinStationDevices(): LiveData<ApiResponse<ResourceData<List<StationDeviceData>>>>

}
