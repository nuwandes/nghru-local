package org.southasia.ghru.jobs

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.commonsware.cwac.saferoom.SQLCipherUtils
import com.commonsware.cwac.saferoom.SafeHelperFactory
import com.google.gson.GsonBuilder
import com.pixplicity.easyprefs.library.Prefs
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.NGRHUDb
import org.southasia.ghru.di.StringConverterFactory
import org.southasia.ghru.util.LiveDataCallAdapterFactory
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class RemoteHouseholdService {

    //private val retrofit: Retrofit
    private val service: NghruService

    init {
        // val tokenManager: TokenManager = TokenManager()
        service = makeRetrofit(accessTokenProvidingInterceptor()).create(NghruService::class.java)

        Log.d("REMOTE_HOUSE_SERVICE", "ACCESS_TOKEN_IS:" + Prefs.getString("ACCESS_TOKEN", null))
    }


    fun makeRetrofit(vararg interceptors: Interceptor) = Retrofit.Builder()
        .baseUrl(Prefs.getString("Ipaddress", BuildConfig.SERVER_URL))
        .client(makeHttpClient(interceptors))
        .addConverters()
        .build()

    fun Retrofit.Builder.addConverters(): Retrofit.Builder {
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        this
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(StringConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
        return this
    }

    private fun makeHttpClient(interceptors: Array<out Interceptor>) = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(headersInterceptor())
        .apply { interceptors().addAll(interceptors) }
        .addInterceptor(loggingInterceptor())
        .build()

    fun accessTokenProvidingInterceptor() = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
//                .addHeader("Authorization", Prefs.getString("ACCESS_TOKEN", null))
                .addHeader("Authorization", Prefs.getString("ACCESS_TOKEN", null))
                .build()
        )
    }

    fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    fun headersInterceptor() = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("Accept-Language", "en")
                .addHeader("Content-Type", "application/json")
                .build()
        )
    }


    @Synchronized
    fun provideDb(app: Context): NGRHUDb {

        // SafeHelperFactory factory=SafeHelperFactory.fromUser(passphraseField.getText());
        val dbname = "nhealth.db"
        if (BuildConfig.DEBUG) {
            return Room
                .databaseBuilder(app, NGRHUDb::class.java, dbname)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        } else {
            val toCharArray = "SafeHelperFactory".toCharArray()
            val factory: SafeHelperFactory = SafeHelperFactory(toCharArray)

            val state = SQLCipherUtils.getDatabaseState(app, dbname)

            if (state == SQLCipherUtils.State.ENCRYPTED) {
                println("ENCRYPTED")
                // SQLCipherUtils.decrypt(app, dbname, "SafeHelperFactory".toCharArray());

            } else {
                SQLCipherUtils.encrypt(app, dbname, toCharArray)
            }


            val build: NGRHUDb = Room
                .databaseBuilder(app, NGRHUDb::class.java, dbname)
                .openHelperFactory(factory)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()

            return build
        }
    }

    @Synchronized
    fun getInstance(): RemoteHouseholdService {
        instance = RemoteHouseholdService()
        return instance as RemoteHouseholdService
    }


    @Throws(IOException::class, RemoteException::class)
    fun addHouseholdRequestMeta(household: HouseholdRequestMeta) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addHouseholdRequestSync(household).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun addMemmber(household: HouseholdRequest, member: List<SyncHouseholdMemberJob.MemberDTO>) {


        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addMemberSync(member, household.enumerationId).execute()
        ////L.d("error remote addMemmber: " + response.toString())

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }
        //Log.d("addMemmber", response.body().toString())

        //L.d("successful remote response: " + response.toString())
    }

    @Throws(IOException::class, RemoteException::class)
    fun addParticipantRequest(participantRequest: ParticipantRequest) {
        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addParticipantRequestSync(participantRequest).execute()
        //L.d("error remote addParticipantRequest: " + response.toString())
        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote addParticipantRequest: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun addBodyMeasurementRequestSync(screeningId: String, bodyMeasurementRequest: BodyMeasurementRequest) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addBodyMeasurementRequestSync(screeningId, bodyMeasurementRequest).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun addBodyMeasurementMetaSync(screeningId: String, bodyMeasurementRequest: BodyMeasurementMeta) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addBodyMeasurementMetaSync(screeningId, bodyMeasurementRequest).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            // L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }

    @Throws(IOException::class, RemoteException::class)
    fun addAxivitySync(screeningId: String, axivity: Axivity) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addAxivitySync(screeningId, axivity).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            // L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //  L.d("successful remote response: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun addSampleRequest(sampleRequest: SampleRequest,sampleCreateRequest : SampleCreateRequest) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addSample(sampleRequest.screeningId!!, sampleRequest.sampleId,sampleCreateRequest).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }

    @Throws(IOException::class, RemoteException::class)
    fun addSampleProcess(sampleProcess: SampleProcess, sampleRequest: SampleRequest?) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addSampleProcess(sampleRequest?.sampleId!!, sampleProcess).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }

    @Throws(IOException::class, RemoteException::class)
    fun addSampleStorageRequest(sampleStorageRequest: SampleStorageRequest) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addSampleStoyage(sampleStorageRequest.sampleId, sampleStorageRequest).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun addParticipantMeta(participantMeta: ParticipantMeta) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addParticipantMeta(participantMeta).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun uploadBackground(participantRequest: ParticipantRequest) {

        val file = File(participantRequest.identityImage)
        val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file)
        val body = MultipartBody.Part.createFormData("image", file.getName(), reqFile)
        val screeningId = RequestBody.create(MediaType.parse("text/plain"), participantRequest.screeningId)
        val subject = RequestBody.create(MediaType.parse("text/plain"), participantRequest.screeningId)
        val patient = RequestBody.create(MediaType.parse("text/plain"), "Screening")
        val id = RequestBody.create(MediaType.parse("text/plain"), "id")
        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.uploadBackground(
            screeningId = screeningId,
            subjectId = subject,
            subjectType = patient,
            purpose = id,
            image = body
        ).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }

    @Throws(IOException::class, RemoteException::class)
    fun uploadConcent(concentPhoto: String?, screeningId: String?) {

        val file = File(concentPhoto)
        val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file)
        val body = MultipartBody.Part.createFormData("image", file.getName(), reqFile)
        val screeningIdV = RequestBody.create(MediaType.parse("text/plain"), screeningId!!)
        val subject = RequestBody.create(MediaType.parse("text/plain"), screeningId.toString())
        val patient = RequestBody.create(MediaType.parse("text/plain"), "Screening")
        val id = RequestBody.create(MediaType.parse("text/plain"), "consent")
        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.uploadBackground(
            screeningId = screeningIdV,
            subjectId = subject,
            subjectType = patient,
            purpose = id,
            image = body
        ).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun addSpirometry( spirometryRequest: SpirometryRequest) {


        val response =
            service.addSpirometry(screeningId = spirometryRequest.screeningId, spirometryData = spirometryRequest)
                .execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun addECG(participantRequest: ParticipantRequest?, eCGStatus: ECGStatus) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addECG(participantRequest?.screeningId!!, eCGStatus).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun addFundoscopy(participantRequest: ParticipantRequest?, fundoscopyRequest:FundoscopyRequest) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response =
            service.addFundoscopy(participantRequest?.screeningId!!, fundoscopyRequest )
                .execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }

    @Throws(IOException::class, RemoteException::class)
    fun addSurvey(survey: QuestionMeta) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addSurvey(survey).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }

    @Throws(IOException::class, RemoteException::class)
    fun addCancelRequestSync(
        participantRequest: ParticipantRequest,
        cancelRequest: CancelRequest
    ) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addCancelRequestSync(participantRequest.screeningId, cancelRequest).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }

    @Throws(IOException::class, RemoteException::class)
    fun addCancelAxivityRequestSync(
        participantRequest: ParticipantRequest,
        cancelRequest: CancelRequest
    ) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addCancelAxivityRequestSync(participantRequest.screeningId, cancelRequest).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun addCancelSampleRequestSync(
        sampleRequest: SampleRequest,
        cancelRequest: CancelRequest
    ) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addCancelSampleRequestSync(sampleRequest.sampleId, cancelRequest).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
//            L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        // L.d("successful remote response: " + response.toString())
    }

    @Throws(IOException::class, RemoteException::class)
    fun addCancelSampleStorageRequestSync(
        sampleRequest: SampleRequest,
        cancelRequest: CancelRequest
    ) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addCancelStorageRequestSync(sampleRequest.storageId!!, cancelRequest).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //  L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }


    @Throws(IOException::class, RemoteException::class)
    fun addStorage(
        sampleStorageRequest: SampleRequest?,
        storageDto: StorageDto
    ) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addStorage(sampleStorageRequest?.storageId!!, storageDto).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }

    companion object {

        private var instance: RemoteHouseholdService? = null
    }

    @Throws(IOException::class, RemoteException::class)
    fun addBloodPressuerMetaRequestSync(screeningId: String, bloodPressureMetaRequest: BloodPressureMetaRequest) {

        // Remote call can be executed synchronously since the job calling it is already backgrounded.]
        val response = service.addBloodPresureRequestSync(screeningId, bloodPressureMetaRequest).execute()

        if (response == null || !response.isSuccessful || response.errorBody() != null) {
            //L.d("error remote response: " + response.toString())
            throw RemoteException(response)
        }

        //L.d("successful remote response: " + response.toString())
    }
}

