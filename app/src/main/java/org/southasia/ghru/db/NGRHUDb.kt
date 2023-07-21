package org.southasia.ghru.db


import androidx.room.Database
import androidx.room.RoomDatabase
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.*
import org.southasia.ghru.vo.request.Member

/**
 * Main database description.
 */
@Database(
    entities = [
        User::class,
        LoginData::class,
        Member::class,
        AccessToken::class,
        HouseholdRequest::class,
        ParticipantRequest::class,
        Asset::class,
        BodyMeasurementRequest::class,
        SampleData::class,
        SampleRequest::class,
        SampleProcess::class,
        SampleStorageRequest::class,
        HouseholdRequestMeta::class,
        Team::class,
        Meta::class,
        ParticipantMeta::class,
        CancelRequest::class,
        StationDeviceData::class,
        BloodPresureRequest::class,
        BloodPressureMetaRequest::class,
        BloodPresureItemRequest::class,
        Questionnaire::class,
        BodyMeasurementMeta::class,
        ECGStatus::class,
        SpirometryRequest::class,
        FundoscopyRequest::class,
        Axivity :: class
    ],
    version = 8,
    exportSchema = true
)
abstract class NGRHUDb : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun loginDataDao(): LoginDataDao

    abstract fun memberDao(): MemberDao

    abstract fun accessTokenDao(): AccessTokenDao

    abstract fun householdRequestDao(): HouseholdRequestDao

    abstract fun participantRequestDao(): ParticipantRequestDao

    abstract fun assetDao(): AssetDao

    abstract fun bodyMeasurementRequestDao(): BodyMeasurementRequestDao

    abstract fun sampleDataDao(): SampleDataDao

    abstract fun sampleRequestDao(): SampleRequestDao

    abstract fun sampleProcessDao(): SampleProcessDao

    abstract fun sampleStorageRequestDao(): SampleStorageRequestDao

    abstract fun householdRequestMetaMetaDao(): HouseholdRequestMetaMetaDao

    abstract fun metaDao(): MetaDao

    abstract fun participantMetaDao(): ParticipantMetaDao

    abstract fun cancelRequestDao(): CancelRequestDao

    abstract fun stationDevicesDao(): StationDevicesDao

    abstract fun bloodPressureMetaRequestDao(): BloodPressureMetaRequestDao

    abstract fun bloodPressureRequestDao(): BloodPresureRequestDao

    abstract fun bloodPressureItemRequestDao(): BloodPresureItemRequestDao

    abstract fun questionnaireDao(): QuestionnaireDao

    abstract fun bodyMeasurementMetaDao(): BodyMeasurementMetaDao

    abstract fun ecgStatusDao() : ECGStatusDao

    abstract fun metaNewDao(): MetaNewDao

    abstract fun spiromentryRequestDao(): SpiromentryRequestDao

    abstract fun fundoscopyRequestDao() : FundoscopyRequestDao

    abstract fun axivityDao() : AxivityDao

}

