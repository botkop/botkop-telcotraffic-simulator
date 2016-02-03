gunzip -c dist/identities.csv.gz > dist/identities.csv
gunzip -c dist/phone_ids.csv.gz > dist/phone_ids.csv

sqlite3 dist/traffic.db <<-EOF

.mode csv

drop table if exists cell_towers;
drop table if exists identities;
drop table if exists phone_ids;

CREATE TABLE cell_towers(
  "radio" TEXT,
  "mcc" INTEGER,
  "net" INTEGER,
  "area" INTEGER,
  "cell" INTEGER,
  "unit" TEXT,
  "lon" FLOAT,
  "lat" FLOAT,
  "range" TEXT,
  "samples" TEXT,
  "changeable" TEXT,
  "created" TEXT,
  "updated" TEXT,
  "averageSignal" TEXT
);
.import dist/cell_towers.csv cell_towers
CREATE INDEX mcc_net_idx on cell_towers(mcc, net);

CREATE TABLE identities(
  "id" INTEGER,
  "Gender" TEXT,
  "NameSet" TEXT,
  "Title" TEXT,
  "GivenName" TEXT,
  "MiddleInitial" TEXT,
  "Surname" TEXT,
  "StreetAddress" TEXT,
  "City" TEXT,
  "State" TEXT,
  "StateFull" TEXT,
  "ZipCode" TEXT,
  "Country" TEXT,
  "CountryFull" TEXT,
  "EmailAddress" TEXT,
  "Username" TEXT,
  "Password" TEXT,
  "BrowserUserAgent" TEXT,
  "TelephoneNumber" TEXT,
  "TelephoneCountryCode" TEXT,
  "MothersMaiden" TEXT,
  "Birthday" TEXT,
  "TropicalZodiac" TEXT,
  "CCType" TEXT,
  "CCNumber" TEXT,
  "CVV2" TEXT,
  "CCExpires" TEXT,
  "NationalID" TEXT,
  "UPS" TEXT,
  "WesternUnionMTCN" TEXT,
  "MoneyGramMTCN" TEXT,
  "Color" TEXT,
  "Occupation" TEXT,
  "Company" TEXT,
  "Vehicle" TEXT,
  "Domain" TEXT,
  "BloodType" TEXT,
  "Pounds" TEXT,
  "Kilograms" TEXT,
  "FeetInches" TEXT,
  "Centimeters" TEXT,
  "GUID" TEXT,
  "Latitude" TEXT,
  "Longitude" TEXT
);
.import dist/identities.csv identities

CREATE TABLE phone_ids(
  "id" INTEGER,
  "imsi" TEXT,
  "msisdn" TEXT,
  "imei" TEXT
);
.import dist/phone_ids.csv phone_ids

EOF

