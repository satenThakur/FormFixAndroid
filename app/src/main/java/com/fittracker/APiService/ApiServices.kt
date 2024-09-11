package com.fittracker.APiService

import com.fittracker.model.OTPResponse
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiServices {
    /**On-Boarding Flow Apis **/

    @POST("/api/v1/generateOtp/{phone}")
    fun requestOtp(@Path(value = "phone", encoded = true) phone: String): Call<OTPResponse>
    @POST("/api/v1/validateOtp")
    fun validateOtp(@Body jsonObject: JsonObject
    ): Call<OTPResponse>
    @POST("/api/v1/signup")
    fun signup(@Body jsonObject: JsonObject
    ): Call<OTPResponse>

    @POST("/api/v1/{request_for}/register")
    fun register(
        @Path(value = "request_for", encoded = true) request_for: String,
        @Body jsonObject: JsonObject
    ): Call<ResponseBody>

    /*Patient Request OTP*/

    /*Login patient*/
    @POST("/api/v1/{request_for}/login")
    fun loginWithUsername(
        @Path(value = "request_for", encoded = true) request_for: String,
        @Body jsonObject: JsonObject
    ): Call<OTPResponse>

    /*patient forgot Password*/
   /* @POST("/api/v1/{request_for}/forgot-password")
    fun forgotPassword(
        @Path(value = "request_for", encoded = true) request_for: String,
        @Body jsonObject: JsonObject
    ): Single<PasswordChangeModel>

    *//*VerifyOTP*//*
    @POST("/api/v1/auth/{request_for}/verify/login/otp")
    fun getOTP(
        @Path(value = "request_for", encoded = true) request_for: String,
        @Body jsonObject: JsonObject
    ): Call<OTPResponse>

    *//**Patient APis **//*
    *//*Get up coming Appointment*//*
    @GET("/api/v1/patient/getAppointmentByAuthKey")
    fun getAppointment(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): Call<AppointmentResponse>

    *//*Get Specialization*//*
    @GET("/api/v1/masterData/specialization")
    fun getSpecialization(): Call<SpecializationResponse>

    *//*get dependent*//*
    @GET("/api/v1/patient/getDependentsByPatientAuthKey")
    fun getDependent(): Call<DependentResponse>

    *//*Delete dependent *//*
    @DELETE("/api/v1/patient/dependents/{id}")
    fun deleteDependent(@Path("id") id: String): Single<SingleClinic>

    *//*Update Dependent Profile*//*
    @Multipart
    @PUT("/api/v1/patient/dependents/{id}")
    fun editDependent(
        @Path("id") id: String,
        @Part fields: List<MultipartBody.Part>
    ): Call<ResponseBody>

    @Multipart
    @POST("/api/v1/patient/dependents")
    fun addNewDependent(@Part fields: List<MultipartBody.Part>): Call<ResponseBody>

    *//*get MedicalDetailsResponse*//*
    @GET("/api/v1/patient/getMedicalDetails/byAuthKey")
    fun getMedicalDetails(): Call<MedicalDetailsResponse>

    *//*get getByPatientAuthKey*//*
    @GET("/api/v1/patient/getByPatientAuthKey")
    fun getPatient(): Call<PatientsModel>

    *//* Get  medical records *//*
    @GET("/api/v1/patient/medical-record/{id}")
    fun getMedicalRecords(@Path("id") Id: String): Call<com.common.features.repository.model.MedicalRecordResponse>

    *//*Delete Medical Details *//*
    @DELETE("/api/v1/patient/medical-record/{id}")
    fun deleteMedicalRecord(@Path("id") id: String): Single<SingleClinic>
    *//*Delete Medical Details *//*
    @DELETE("/api/v1/patient/deleteMedicalDetails/{id}")
    fun deleteMedicalDetails(@Path("id") id: String): Single<SingleClinic>

    *//*Get NearBy Pharmacy*//*
    @POST("/api/v1/patient/medicalDetails")
    fun addMedicalDetail(@Body jsonObject: JsonObject): Call<ResponseBody>
    *//*get TOP Doctors, Best Clinic*//*
    @POST("/api/v1/doctor/doctorSearchV1")
    fun getTopDocAndBestClinic(@Body jsonObject: JsonObject): Call<DoctorsResponse>

    *//*Get NearBy Pharmacy*//*
    @POST("/api/v1/pharmacyStore/getPharmacyByLocation")
    fun getNearByPharmacy(@Body jsonObject: JsonObject): Call<ResponseBody>

    *//*Get recent orders*//*
    @GET("/api/v1/patient/order")
    fun getRecentOrders(
        @Query("dateFrom") date: String,
        @Query("dateTo") dat: String
    ): Call<ResponseBody>

    *//* Get  medical records *//*
    @GET("/api/v1/patient/medical-record/download/{id}")
    fun getMedicalRecordAttachments(@Path("id") Id: String): Call<MedicalRecordAttachmentResponse>

    *//*Get NearBy Pharmacy*//*
    @POST("/api/v1/pay/createTransactionByOrderId")
    fun createTransactionByOrderId(@Body jsonObject: JsonObject): Call<ResponseBody>

    @GET("https://services.eazyop.in/api/v1/masterData/qualification")
    fun getQualifications(@Query("pageSize") pageSize: String): Single<QualificationModel>


    *//*get MedicalDetailsResponse*//*
    @GET("/api/v1/patient/favouriteDoctorMapping/")
    fun getFavourites(): Call<DoctorsResponse>


    *//*Remove from favourite Doctors*//*
    @DELETE("/api/v1/patient/favouriteDoctorMapping/{id}")
    fun removeFavourite(@Path("id") dockey: String): Call<ResponseBody>

    *//*Add to favourite Doctors*//*
    @POST("/api/v1/patient/favouriteDoctorMapping")
    fun addTofavouriteDoctor(@Body jsonObject: JsonObject): Call<ResponseBody>











    *//**Pharmacy APIs    **//*
*//*    *//**//*Register pharmacy*//**//*
    @POST("/api/v1/pharmacy/register")
    fun getRegisterPharmacy(@Body jsonObject: JsonObject): Call<ResponseBody>
    *//**//*Login pharmacy*//**//*
    @POST("/api/v1/pharmacy/login")
    fun getLoginUsername(@Body jsonObject: JsonObject): Call<OTPResponse>
    *//**//*patient forgot Password*//**//*
    @POST("/api/v1/pharmacy/forgot-password")
    fun forgotPassword(@Body jsonObject: JsonObject): Single<PasswordChangeModel>

    *//**//*Register Doc verify OTP*//**//*
    @POST("/api/v1//pharmacy/verify/register/otp")
    fun getRegisterOTP(@Body jsonObject: JsonObject): Call<OTPResponse>
    *//**//*Change Password*//**//*
    @POST("/api/v1/pharmacy/change-password")
    fun changePassword(@Body jsonObject: JsonObject): Single<PasswordChangeModel>
    *//**//*VerifyOTP*//**//*
    @POST("/api/v1/pharmacy/verify/login/otp")
    fun getOTP(@Body jsonObject: JsonObject): Call<OTPResponse>*//*


    *//**Doctor APIs    **//*

    *//*Login*//*
    *//*  @POST("/api/v1/auth/doctor/login")
      fun getLogin(@Body jsonObject: JsonObject): Call<ResponseBody>

      *//**//*Login*//**//*
    @POST("/api/v1/auth/doctor/login")
    fun getLoginUsername(@Body jsonObject: JsonObject): Call<OTPResponse>

    *//**//*Doctor forgot Password*//**//*
    @POST("/api/v1/auth/doctor/forgot-password")
    fun forgotPassword(@Body jsonObject: JsonObject): Single<PasswordChangeModel>*//*


    /////


    *//*Get Refresh Token*//*
    @POST("/api/v1/refreshToken")
    fun getRefreshToken(): Call<OTPResponse>

    *//*Register Doc*//*
    @POST("/api/v1/auth/doctor/register")
    fun getRegisterDoc(@Body jsonObject: JsonObject): Call<ResponseBody>

    *//*Register Doc verify OTP*//*
    @POST("/api/v1//auth/doctor/verify/register/otp")
    fun getRegisterOTP(@Body jsonObject: JsonObject): Call<OTPResponse>


    *//*Change Password*//*
    @POST("/api/v1/auth/doctor/change-password")
    fun changePassword(@Body jsonObject: JsonObject): Single<PasswordChangeModel>


    *//*Get Doctor PRofile*//*

    @GET("/api/v1/doctor/profile")
    fun getDoctorProfile(): Single<ProfileResponse>

    @GET("/api/v1/doctor/profile/{id}")
    fun getDoctorProfile(@Path("id") id: String): Single<DoctorProfile>


    @GET("/api/v1/patient/search")
    fun getPatientsData(@Query("mobileNumber") mobileNumber: String): Single<PatientInfo>


    @GET("/api/v1/doctor/location")
    fun getDocLocation(): Single<LocationClinic>

    *//*My Locations*//*
    @GET("/api/v1/doctor/location/{docKey}")
    fun getMyLocations(@Path("docKey") docKey: String): Single<MyLocationModel>

    *//*Delete location*//*
    @DELETE("/api/v1/doctor/clinic/{id}")
    fun deleteClinic(@Path("id") clinicKey: String): Single<SingleClinic>

    *//* Doctor appointments*//*
    @GET("/api/v1/doctor/appointment/{docKey}")
    fun getAppointments(
        @Path("docKey") docKey: String, @Query("clinicId") clinicId: String,
        @Query("dateFrom") dateFrom: String, @Query("dateTo") dateTo: String
    ): Single<AppointmentModel>

    @GET("/api/v1/doctor/available-timeslot")
    fun getDocTimeslot(
        @Query("doctorKey") docKey: String, @Query("clinicKey") clinicKey: String,
        @Query("date") date: String
    ): Single<TimeslotDoc>

    @GET("/api/v1/doctor/timeslot-configuration")
    fun getDocTimeslotForDay(
        @Query("doctorKey") doctorKey: String,
        @Query("clinicId") clinicKey: String,
        @Query("day") day: String
    ): Single<TimeslotDoc>

    @GET("/api/v1/doctor/timeslot-configuration")
    fun getDocTimeslotForAllDays(
        @Query("doctorKey") doctorKey: String,
        @Query("clinicId") clinicKey: String
    ): Single<TimeSlotsWithDayModel>

    *//*Unavailablity Timing of Doctors*//*
    @GET("/api/v1/doctor/unavailability-configuration")
    fun getUnavailabilityTiming(
        @Query("doctorKey") doctorId: String,
        @Query("clinicId") clinicId: String,
        @Query("date") date: String
    ): Single<UnavailableTimingModel>


    *//*Update Timing of Doctors*//*
    @POST("/api/v1/doctor/unavailability-configuration/{doctorKey}")
    fun addUnavailabilityTimeConfig(
        @Path("doctorKey") doctorId: String,
        @Body jsonArray: JsonArray
    ): Single<UnavailableTimingModel>

    @PUT("/api/v1/doctor/unavailability-configuration/{doctorKey}")
    fun updateUnavailabilityTimeConfig(
        @Path("doctorKey") doctorId: String,
        @Body jsonArray: JsonArray
    ): Single<UnavailableTimingModel>

    *//* Creates Timeslot-configuration of Doctor *//*
    @POST("/api/v1/doctor/timeslot-configuration/{doctorKey}")
    fun addTimeSlot(
        @Path("doctorKey") doctorKey: String,
        @Body jsonArray: JsonArray
    ): Single<CommonResponse>

    *//* Update Timeslot-configuration of Doctor *//*
    @PUT("/api/v1/doctor/timeslot-configuration/{doctorKey}")
    fun updateTimeSlot(
        @Path("doctorKey") doctorKey: String,
        @Body jsonArray: JsonArray
    ): Single<CommonResponse>

    *//* Delete a Timeslot using Id *//*
    @DELETE("/api/v1/doctor/timeslot-configuration/{id}")
    fun deleteTimeSlot(@Path("id") id: String): Single<SimpleResponse>

    *//*MyPatients*//*
    @GET("/api/v1/doctor/myPatients")
    fun getMyPatients(): Single<PatientsModel>

    @GET("/api/v1/patient/{id}")
    fun getPatient(@Path("id") id: String): Single<PatientModel>

    *//* Get particular patient latest medical records *//*
    @GET("/api/v1/patient/getMedicalDetails/latest/{patientKey}")
    fun getPatientLatestMedicalRecords(@Path("patientKey") patientKey: String): Single<MyPatientLatestMedicalRecordModel>

    *//* Get particular patient medical records *//*
    @GET("/api/v1/patient/medical-record/byPatientId/{id}")
    fun getSelectedPatientMedicalRecords(@Path("id") Id: String): Single<MyPatientMedicalRecordModel>

    @GET("/api/v1/patient/medical-record/download/{id}")
    fun getMedicalRecordForDownload(@Path("id") Id: String): Single<MyPatientMedicalDownloadRecordModel>

    @GET("/api/v1/masterData/medicalRecordtype")
    fun getMedicalRecordTypes(): Single<MedicalRecordModel>

    @Multipart
    @POST("/api/v1/patient/medical-record")
    fun addNewMedicalRecord(
        @Part fields: List<MultipartBody.Part>,
        @Part attachments: List<MultipartBody.Part>
    ): Single<MyPatientNewMedicalRecordModel>
    @Multipart
    @POST("/api/v1/patient/medical-record")
    fun addMedicalRecord(
        @Part fields: List<MultipartBody.Part>,
       *//* @Part attachments: List<MultipartBody.Part>*//*
    ): Single<MyPatientNewMedicalRecordModel>
    @POST("/api/v1/patient/medicalDetails")
    fun addPatientDetails(@Body jsonObject: JsonObject): Call<ResponseBody>

    *//*Reviews*//*
    @GET("/api/v1/doctor/reviewsByDocAuthKey")
    fun getReviews(): Single<ReviewModel>

    @PUT("/api/v1/doctor/review/saveReply/{id}")
    fun saveReviewDoctorReply(
        @Path("id") id: String,
        @Body jsonObject: JsonObject
    ): Single<CommonResponse>

    *//*Support*//*

    // gets all Issue Types
    @GET("/api/v1/masterData/issuetype")
    fun getIssueTypes(): Single<IssueTypeModel>

    // Get all tickets of the logged in user
    @GET("/api/v1/support/tickets/ownedBy")
    fun getSupportList(): Single<SupportModel>

    // create a new support ticket
    @Multipart
    @POST("/api/v1/support/ticket")
    fun createNewSupportTicket(
        @Part fields: List<MultipartBody.Part>,
        @Part attachments: List<MultipartBody.Part>
    ): Single<SupportTicketModel>

    // Get support ticket
    @GET("/api/v1/support/ticket/{id}")
    fun getSupportTicket(@Path("id") Id: String): Single<SupportTicketModel>

    @PUT("/api/v1/support/ticket/addComment/{id}")
    fun addCommentToSupportTicket(
        @Path("id") Id: String,
        @Body jsonObject: JsonObject
    ): Single<SupportTicketModel>

    *//*Clinic Manager*//*
    @GET("/api/v1/auth/doctor/clinicManagerByClinicKey/{clinicKey}")
    fun getClinicManagerList(@Path("clinicKey") clinicKey: String): Single<ClinicManagerModel>

    *//*Update Profile*//*
    @Multipart
    @PUT("/api/v1/doctor/profile-update")
    fun updateProfile(
        @Part fields: List<MultipartBody.Part>
    ): Single<ProfileResponse>

    *//* Clinic location *//*
    @GET("/api/v1/doctor/clinic/{id}")
    fun getClinic(@Path("id") id: String): Single<SingleClinic>

    @Multipart
    @POST("/api/v1/doctor/clinic/{docId}")
    fun createDoctorClinic(
        @Path("docId") docId: String,
        @Part fields: List<MultipartBody.Part>,
        @Part photos: Array<MultipartBody.Part>
    ): Single<SingleClinic>

    @Multipart
    @PUT("/api/v1/doctor/clinic/{id}")
    fun updateDoctorClinic(
        @Path("id") id: String,
        @Part fields: List<MultipartBody.Part>,
        @Part photos: Array<MultipartBody.Part>
    ): Single<SingleClinic>

    *//* End of Clinic location *//*

    @GET("/api/v1/doctor/getAppointmentsByDocAuthKey")
    fun getCreateAppointmentTimeSlots(
        @Query("clinicId") clinicId: String,
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String,
        @Query("appointmentType") appointmentType: String
    ): Single<AppointmentModel>

    @GET("/api/v1/doctor/getAppointmentsByDocAuthKey")
    fun getCreateAllTypeAppointmentTimeSlots(
        @Query("clinicId") clinicId: String,
        @Query("dateFrom") dateFrom: String, @Query("dateTo") dateTo: String
    ): Single<AppointmentModel>

    *//*Search Globally with key*//*
    // TODO: Need to add page size and page number.
    @GET("/api/v1/doctor/myPatients")
    fun searchForPatientsForAutoSuggest(@Query("searchKey") searchKey: String): Single<PatientsSearchModel>

    *//*Create Appoitment *//*
    @POST("/api/v1/patient/appointment")
    fun createAppointment(@Body jsonObject: JsonObject): Call<ResponseBody>

    *//*Create Appoitment *//*
    @POST("/api/v1/patient/appointment")
    fun createPatientAppointment(@Body jsonObject: JsonObject): Call<CreateAppointmentResponse>

    *//* Cancel patient Appointment*//*
    @PUT("/api/v1/patient/cancelAppointment/{appointmentId}")
    fun cancelAppointment(@Path("appointmentId") appointmentId: String): Single<SingleAppointmentModel>

    *//* Get particular patient all appointments *//*
    @GET("/api/v1/patient/appointment/{patientKey}")
    fun getSelectedPatientAllAppointments(
        @Path("patientKey") patientKey: String,
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): Single<AppointmentModel>

    *//* Get particular patient all prescriptions *//*
    @GET("/api/v1/patients/prescription/{patientKey}")
    fun getSelectedPatientAllPrescriptions(@Path("patientKey") patientKey: String): Single<PrescriptionSingleModel>

    @POST("/api/v1/patient/registerDependentsByDoctor")
    fun registerDependentsByDoctor(@Body jsonObject: JsonObject): Single<PatientModel>

    @POST("/api/v1/auth/patient/registerBydoctor")
    fun registerNewPatientByDoctor(@Body jsonObject: JsonObject): Single<PatientModel>

    // Prescription
    *//*To get the Medicines *//*
    @GET("/api/v1/clinic/{clinicId}/medicines")
    fun getMedicinesByClinic(
        @Path("clinicId") clinicId: String,
        @Query("pageNumber") pageNumber: String,
        @Query("pageSize") pageSize: String,
        @Query("searchValue") searchValue: String
    ): Single<MedicineModel>

    *//*To create prescription by doctor *//*
    @POST("/api/v1/doctor/prescription")
    fun createPrescriptionByDoctor(@Body jsonObject: JsonObject): Single<PrescriptionModel>

    *//* Get prescription by id *//*
    @GET("/api/v1/patients/prescription/Id/{id}")
    fun getPrescriptionById(@Path("id") id: String): Single<PrescriptionDetailDisplayModel>

    *//*Add Education*//*
    @PUT("/api/v1/doctor/field-update")
    fun updateProfileField(@Body jsonObject: JsonObject): Single<ProfileResponse>



    *//*Register Council*//*
    @GET("https://services.eazyop.in/api/v1/masterData/registerCouncil")
    fun getRegisterCounsil(
        @Query("pageNumber") pageNumber: String,
        @Query("pageSize") pageSize: String
    ): Single<RegisterCounsilModel>

    *//*Register Council*//*
    @GET("https://services.eazyop.in/api/v1/masterData/specialization")
    fun getspecialization(
        @Query("pageNumber") pageNumber: String,
        @Query("pageSize") pageSize: String
    ): Single<SpecializeModel>

    *//*Accounts*//*
    @POST("/api/v1/doctor/accounts")
    fun addMyBankAccount(@Body jsonObject: JsonObject): Single<AddBankAccountResponsedata>

    @PUT("/api/v1/doctor/accounts/{accountId}")
    fun updateMyBankAccount(
        @Path("accountId") accountId: String,
        @Body jsonObject: JsonObject
    ): Single<MyAccountsModel>

    @GET("/api/v1/doctor/accounts")
    fun getMyBankAccount(): Single<MyAccountsModel>

    @DELETE("/api/v1/doctor/accounts/{accountId}")
    fun deleteMyBankAccount(@Path("accountId") accountId: String): Single<MyAccountsModel>

    @GET("/api/v1/doctor/payment-transaction")
    fun getTransactions(@Query("clinicKey") clinicKey: String): Single<AccountTransactionModel>

    @POST("/api/v1/doctor/payment-settings")
    fun addPaymentSetting(@Body jsonObject: JsonObject): Single<PaymentSettingModel>

    @GET("/api/v1/doctor/payment-settings/{clinicKey}")
    fun getPaymentSetting(@Path("clinicKey") clinicKey: String): Single<PaymentSettingsArrayModel>*/
}
