package kz.mobile.mgov.profile.data.api

import com.google.gson.JsonObject
import io.reactivex.Single
import kz.mobile.mgov.document.data.model.response.*
import kz.mobile.mgov.profile.data.model.response.*
import retrofit2.Response
import retrofit2.http.*

interface ProfileApi {

    @GET("person-profile/rest-v2/profile/")
    fun getProfileData(
        @Header("cookie") cookie: String
    ): Single<Response<ProfileResponse>>

    @GET("nss/rest-v2/settings/contact/mail")
    fun getProfileEmail(
        @Header("cookie") cookie: String
    ): Single<Response<JsonObject>>

    @GET("nss/rest-v2/settings/mbc-phone")
    fun getProfilePhone(
        @Header("cookie") cookie: String
    ): Single<Response<JsonObject>>


    @GET("/store-docs/document-list")
    fun getElectronicDocuments(
        @Header("code") code: String,
        @QueryMap map: Map<String, String>
    ): Single<Response<MyDocumentsResponse>>


    @GET("/pc-v2-adapter/documents")
    fun getDocuments(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<DocumentsListResponses>>

    @GET("/pc-v2-adapter/timeline")
    fun getTimeline(@Header("code") code: String,
                    @Query("ticket") ticket: String
    ): Single<Response<TimelineResponse>>

    @GET("/pc-v2-adapter/recommender")
    fun getInterestingServices(
        @Header("code") code: String,
        @Query("serviceType") type: String = "mgov",
        @Query("ticket") ticket: String
    ): Single<Response<JsonObject>>

    @GET("/pc-v2-adapter/addresses")
    fun getAddresses(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<AddressResponse>

    @GET("/pc-v2-adapter/subscriptions-info")
    fun getDocumentsIds(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<JsonObject>>

    @GET("/pc-v2-adapter/legal-entity-participants")
    fun getLegalEntityParticipant(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<LegalEntityResponse>>


    @GET("/pc-v2-adapter/social-statuses")
    fun getSocialStatus(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<SocialStatusResponse>>


    @GET("/pc-v2-adapter/marriage")
    fun getMarriage(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<MarriageResponse>>


    @GET("/pc-v2-adapter/driver-licenses")
    fun getDriverLicense(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<DriverLicenseResponse>>


    @GET("/pc-v2-adapter/realties")
    fun getProperty(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<JsonObject>>


    @GET("/pc-v2-adapter/licenses")
    fun getLicenses(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<LicensesResponse>>

    @GET("/pc-v2-adapter/transports")
    fun getTransport(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<AutoResponse>>


    @GET("/pc-v2-adapter/penalties")
    fun getTransportFines(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<TransportPenaltyResponse>>


    @GET("/pc-v2-adapter/clinic-attachment")
    fun getClinicAttachments(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<ClinicResponse>>


    @GET("/pc-v2-adapter/individual-entrepreneur")
    fun getIndividualEntrepreneurInfo(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<IndividualEntrepreneurResponse>>


    @GET("/pc-v2-adapter/children")
    fun getChildInfo(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<ChildrenResponse>>


    @GET("/pc-v2-adapter/msign")
    fun getMSignInfo(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<JsonObject>>


    @GET("/pc-v2-adapter/administrative-penalty")
    fun getGovFinesInfo(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<DebtorRegisterResponse>>

    @GET("/pc-v2-adapter/government-employee")
    fun getGovServicesInfo(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<JsonObject>>


    @GET("/pc-v2-adapter/accommodation-queue")
    fun getAccommodationQueue(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<AccommodationQueueResponse>>

    @GET("/pc-v2-adapter/avatar")
    fun getAvatar(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<JsonObject>>


    @POST("/pc-v2-adapter/avatar")
    fun setAvatar(
        @Header("code") code: String,
        @Query("ticket") ticket: String,
        @Body body: JsonObject
    ): Single<Response<JsonObject>>

    @DELETE("/pc-v2-adapter/avatar")
    fun deleteAvatar(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<JsonObject>>

    @GET("/pc-v2-adapter/subscriptions-info")
    fun userServicesSectionsInfo(
        @Header("code") code: String,
        @Query("ticket") ticket: String
    ): Single<Response<SubscriptionInfoResponse>>

    @PUT("/pc-v2-adapter/subscribe/{section}")
    fun subscribeToSection(
        @Header("code") code: String,
        @Path("section") section: String,
        @Query("ticket") ticket: String
    ): Single<Response<JsonObject>>

    @PUT("/pc-v2-adapter/unsubscribe/{section}")
    fun unsubscribeFromSection(
        @Header("code") code: String,
        @Path("section") section: String,
        @Query("ticket") ticket: String
    ): Single<Response<JsonObject>>
}