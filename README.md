# Vaccine-Scheduler
A simulation of registering for a COVID-19 vaccine appointment. It already includes the 3 main vaccines (J&amp;J, Pfizer, Moderna), but caregivers can create new ones. This project was created mainly to enchance my learning of database applications including using SQL in java with ER diagrams, data normalization, and SQL queries / updates.

# Guide
Once the user runs the program, these are all the possible options that can be done (assuming they are allowed to). I will run through what each option does.

- create_patient and create_caregiver allows the user to create a patient to receive the vaccine or a caregiver to adminster it.
- login_patient and login_caregiver allows the user to login as an existing patient and caregiver.
- search_caregiver_schedule allows a caregiver or patient to search for caregivers available on the given date as well as the number of doses of each vaccine left.
- reserve allows a patient to reserve a valid date and vaccine (assuming there are doses left) for an appointment with a caregiver that day. The caregiver is chosen in ascending alphabetical order.
- upload_availability allows caregivers to upload a date when they are available for patients to make an appointment with them.
- cancel allows both patients and caregivers to cancel a valid date they have an appointment on.
- show_all_available_dates shows all available dates for every caregiver.
- add_doses allows caregivers to add doses to existing vaccines or to create a new vaccine (real or fiction).
- get_vaccine_information displays all existing vaccines in the database with their number of doses remaining.
- show_appointments shows appointments for the logged in patient or caregiver
- logout is self-explanatory
- help displays the main menu again. Note that the menu will not print again after commands are entered so that information is not lost by the menu being printed a lot of times.
- quit terminates the program.

# Disclaimer
This program is not affilated with any governmental program / agency and should not be taken seriously as a source of information related to COVID-19 or as medical advice. Please visit an official site such as vaccines.gov to schedule an actual appointment or seek verified medical advice.
