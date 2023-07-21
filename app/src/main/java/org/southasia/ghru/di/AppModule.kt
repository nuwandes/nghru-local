package org.southasia.ghru.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.birbit.android.jobqueue.JobManager
import com.commonsware.cwac.saferoom.SQLCipherUtils
import com.commonsware.cwac.saferoom.SafeHelperFactory
import com.crashlytics.android.Crashlytics
import com.google.gson.GsonBuilder
import com.pixplicity.easyprefs.library.Prefs
import com.squareup.otto.Bus
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.api.NghruServiceLocal
import org.southasia.ghru.db.*
import org.southasia.ghru.jobs.GcmJobService
import org.southasia.ghru.jobs.JobManagerFactory
import org.southasia.ghru.jobs.SchedulerJobService
import org.southasia.ghru.sync.*
import org.southasia.ghru.util.LiveDataCallAdapterFactory
import org.southasia.ghru.util.TokenManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module(includes = [ViewModelModule::class])
class AppModule {
    @Singleton
    @Provides
    @Synchronized
    fun provideNghruService(tokenManager: TokenManager): NghruService {
        return makeRetrofit(accessTokenProvidingInterceptor(tokenManager)).create(NghruService::class.java)
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
        .connectTimeout(60, TimeUnit.MINUTES)
        .readTimeout(60, TimeUnit.MINUTES)
        .addInterceptor(headersInterceptor())
        .apply { interceptors().addAll(interceptors) }
        .addInterceptor(loggingInterceptor())
        .build()

    fun accessTokenProvidingInterceptor(tokenManager: TokenManager) = Interceptor { chain ->
        //  val accessToken = Pref.token
        Crashlytics.setUserEmail(tokenManager.getEmail())
        val token = tokenManager.getToken().accessToken
        chain.proceed(
            chain.request().newBuilder()
                .addHeader("Authorization", "$token")
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

    @Singleton
    @Provides
    @Synchronized
    fun provideRetrofit(): Retrofit {

        val interceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        }

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .excludeFieldsWithoutExposeAnnotation()
            .create()
        //2018-02-26 11:32:47
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        //https://solid-space-200111.appspot.com/ google cloud
        return Retrofit.Builder()
            .baseUrl(Prefs.getString("Ipaddress", BuildConfig.SERVER_URL))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(StringConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE sample_request ADD COLUMN is_cancelled INTEGER")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE participant_meta ADD COLUMN country_code TEXT")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER table questionnaire ADD  COLUMN new_id integer auto_increment")
        }
    }



    @Singleton
    @Provides
    fun provideNghruServiceLocal(): NghruServiceLocal {

        val interceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        }

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()
        //2018-02-26 11:32:47
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        return Retrofit.Builder()
            .baseUrl("http://192.168.4.22/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(StringConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(NghruServiceLocal::class.java)
    }

    @Singleton
    @Provides
    @Synchronized
    fun configureJobManager(): JobManager {
        return JobManagerFactory.getJobManager()
    }

    @Singleton
    @Provides
    @Synchronized
    fun provideDb(app: Application): NGRHUDb {

        // SafeHelperFactory factory=SafeHelperFactory.fromUser(passphraseField.getText());
        val dbname = "nhealth.db"
        if (BuildConfig.DEBUG) {
            return Room
                .databaseBuilder(app, NGRHUDb::class.java, dbname)
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        } else {
            val toCharArray = "SafeHelperFactory".toCharArray()
            var factory: SafeHelperFactory = SafeHelperFactory(toCharArray)

            val state = SQLCipherUtils.getDatabaseState(app, dbname)

            if (state == SQLCipherUtils.State.ENCRYPTED) {
                println("ENCRYPTED")
                // SQLCipherUtils.decrypt(app, dbname, "SafeHelperFactory".toCharArray());

            } else {
                SQLCipherUtils.encrypt(app, dbname, toCharArray)
            }


            val build: NGRHUDb = Room
                .databaseBuilder(app, NGRHUDb::class.java, dbname)
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_5_6)
                .openHelperFactory(factory)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()

            return build
        }
    }

    @Singleton
    @Provides
    fun provideSchedulerJobService(): SchedulerJobService {
        return SchedulerJobService()
    }

    @Singleton
    @Provides
    fun provideGcmJobService(): GcmJobService {
        return GcmJobService()
    }


    @Singleton
    @Provides
    fun provideUserDao(db: NGRHUDb): UserDao {
        return db.userDao()
    }

    @Singleton
    @Provides
    fun provideQuestionnaireDao(db: NGRHUDb): QuestionnaireDao {
        return db.questionnaireDao()
    }


    @Singleton
    @Provides
    fun provideSampleRequestDao(db: NGRHUDb): SampleRequestDao {
        return db.sampleRequestDao()
    }

    @Singleton
    @Provides
    fun provideSampleDataDao(db: NGRHUDb): SampleDataDao {
        return db.sampleDataDao()
    }

    @Singleton
    @Provides
    fun provideHouseholdRequestRequestDao(db: NGRHUDb): HouseholdRequestDao {
        return db.householdRequestDao()
    }


    @Singleton
    @Provides
    fun provideAccessTokenDao(db: NGRHUDb): AccessTokenDao {
        return db.accessTokenDao()
    }

    @Singleton
    @Provides
    fun provideSampleStorageRequestDao(db: NGRHUDb): SampleStorageRequestDao {
        return db.sampleStorageRequestDao()
    }


    @Singleton
    @Provides
    fun providebodyMeasurementRequestDao(db: NGRHUDb): BodyMeasurementRequestDao {
        return db.bodyMeasurementRequestDao()
    }

    @Singleton
    @Provides
    fun provideSampleProcessDao(db: NGRHUDb): SampleProcessDao {
        return db.sampleProcessDao()
    }

    @Singleton
    @Provides
    fun provideHouseholdRequestMetaMetaDao(db: NGRHUDb): HouseholdRequestMetaMetaDao {
        return db.householdRequestMetaMetaDao()
    }

    @Singleton
    @Provides
    fun provideMetaDao(db: NGRHUDb): MetaDao {
        return db.metaDao()
    }

    @Singleton
    @Provides
    fun provideParticipantMetaDao(db: NGRHUDb): ParticipantMetaDao {
        return db.participantMetaDao()
    }

    @Singleton
    @Provides
    fun provideBodyMeasurementMetaDao(db: NGRHUDb): BodyMeasurementMetaDao {
        return db.bodyMeasurementMetaDao()
    }



    @Singleton
    @Provides
    fun provideBus(): Bus {
        return Bus()
    }

    @Singleton
    @Provides
    fun provideLoginDataDao(db: NGRHUDb): LoginDataDao {
        return db.loginDataDao()
    }

    @Singleton
    @Provides
    fun provideMemberDao(db: NGRHUDb): MemberDao {
        return db.memberDao()
    }

    @Singleton
    @Provides
    fun provideAssetDao(db: NGRHUDb): AssetDao {
        return db.assetDao()
    }


    @Singleton
    @Provides
    fun provideParticipantRequestDao(db: NGRHUDb): ParticipantRequestDao {
        return db.participantRequestDao()
    }

    @Singleton
    @Provides
    fun provideCancelRequestDao(db: NGRHUDb): CancelRequestDao {
        return db.cancelRequestDao()
    }

    @Singleton
    @Provides
    fun provideStationDevicesDao(db: NGRHUDb): StationDevicesDao {
        return db.stationDevicesDao()
    }

    @Singleton
    @Provides
    fun provideSpiromentryRequestDao(db: NGRHUDb): SpiromentryRequestDao {
        return db.spiromentryRequestDao()
    }


    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app

//    @Provides
//    @Singleton
//    fun provideL(app: Application): L = L(app)

    @Provides
    internal fun provideSyncHouseholdLifecycleObserver(householdDao: HouseholdRequestDao): SyncHouseholdLifecycleObserver {
        return SyncHouseholdLifecycleObserver(householdDao)
    }

    @Provides
    internal fun provideSyncHouseholdMemberLifecycleObserver(memberDao: MemberDao): SyncHouseholdMemberLifecycleObserver {
        return SyncHouseholdMemberLifecycleObserver(memberDao)
    }

    @Provides
    internal fun provideSpirometryRequestRxBusLifecycleObserver(spiromentryRequestDao: SpiromentryRequestDao): SpirometryRequestRxBusLifecycleObserver {
        return SpirometryRequestRxBusLifecycleObserver(spiromentryRequestDao)
    }



    @Provides
    internal fun provideSyncParticipantRequestLifecycleObserver(articipantRequestDao: ParticipantRequestDao): SyncParticipantRequestLifecycleObserver {
        return SyncParticipantRequestLifecycleObserver(articipantRequestDao)
    }

//    @Provides
//    internal fun provideBodyMeasurementRequestLifecycleObserver(bodyMeasurementRequestDao: BodyMeasurementRequestDao): BodyMeasurementRequestLifecycleObserver {
//        return BodyMeasurementRequestLifecycleObserver(bodyMeasurementRequestDao)
//    }


    @Singleton
    @Provides
    fun provideSharedPreferences(app: Context): SharedPreferences {
        return app.getSharedPreferences("prefs", 0)// private mode
    }

    @Singleton
    @Provides
    fun provideBloodPressureMetaRequestDao(db: NGRHUDb): BloodPressureMetaRequestDao {
        return db.bloodPressureMetaRequestDao()
    }

    @Singleton
    @Provides
    fun provideBloodPressureRequestDao(db: NGRHUDb): BloodPresureRequestDao {
        return db.bloodPressureRequestDao()
    }

    @Singleton
    @Provides
    fun provideBloodPressureItemRequestDao(db: NGRHUDb): BloodPresureItemRequestDao {
        return db.bloodPressureItemRequestDao()
    }
    @Singleton
    @Provides
    fun provideECGStatusDao(db: NGRHUDb): ECGStatusDao {
        return db.ecgStatusDao()
    }
    @Singleton
    @Provides
    fun provideMetaNewsDao(db: NGRHUDb): MetaNewDao {
        return db.metaNewDao()
    }

    @Singleton
    @Provides
    fun provideFundoscopyRequestDao(db: NGRHUDb): FundoscopyRequestDao {
        return db.fundoscopyRequestDao()
    }

    @Singleton
    @Provides
    fun provideAxivityDao(db: NGRHUDb): AxivityDao {
        return db.axivityDao()
    }
    @Provides
    internal fun provideBloodPressureMetaRequestLifecycleObserver(bloodPressureMetaRequestDao: BloodPresureRequestDao): BloodPressureMetaRequestLifecycleObserver {
        return BloodPressureMetaRequestLifecycleObserver(bloodPressureMetaRequestDao)
    }

}
