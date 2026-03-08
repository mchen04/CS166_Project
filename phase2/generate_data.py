#!/usr/bin/env python3
"""generate dummy csv data for the mechanic shop database."""

import csv
import random
import string
import os
from datetime import date, timedelta

random.seed(42)

OUT_DIR = os.path.join(os.path.dirname(__file__), "data")
os.makedirs(OUT_DIR, exist_ok=True)

# --- name pools ---

FIRST_NAMES = [
    "james", "mary", "john", "patricia", "robert", "jennifer", "michael",
    "linda", "david", "elizabeth", "william", "barbara", "richard", "susan",
    "joseph", "jessica", "thomas", "sarah", "charles", "karen", "chris",
    "daniel", "lisa", "matthew", "nancy", "anthony", "betty", "mark",
    "margaret", "donald", "sandra", "steven", "ashley", "paul", "dorothy",
    "andrew", "kimberly", "joshua", "emily", "kenneth", "donna", "kevin",
    "michelle", "brian", "carol", "george", "amanda", "timothy", "melissa",
    "ronald", "deborah", "edward", "stephanie", "jason", "rebecca", "jeff",
    "sharon", "ryan", "laura", "jacob", "cynthia", "gary", "kathleen",
    "nicholas", "amy", "eric", "angela", "jonathan", "shirley", "stephen",
    "anna", "larry", "brenda", "justin", "pamela", "scott", "emma",
    "brandon", "nicole", "benjamin", "helen", "samuel", "samantha", "raymond",
    "katherine", "gregory", "christine", "frank", "debra", "alexander", "rachel",
    "patrick", "carolyn", "jack", "janet", "dennis", "catherine", "jerry", "maria"
]

LAST_NAMES = [
    "smith", "johnson", "williams", "brown", "jones", "garcia", "miller",
    "davis", "rodriguez", "martinez", "hernandez", "lopez", "gonzalez",
    "wilson", "anderson", "thomas", "taylor", "moore", "jackson", "martin",
    "lee", "perez", "thompson", "white", "harris", "sanchez", "clark",
    "ramirez", "lewis", "robinson", "walker", "young", "allen", "king",
    "wright", "scott", "torres", "nguyen", "hill", "flores", "green",
    "adams", "nelson", "baker", "hall", "rivera", "campbell", "mitchell",
    "carter", "roberts", "gomez", "phillips", "evans", "turner", "diaz",
    "parker", "cruz", "edwards", "collins", "reyes", "stewart", "morris",
    "morales", "murphy", "cook", "rogers", "gutierrez", "ortiz", "morgan",
    "cooper", "peterson", "bailey", "reed", "kelly", "howard", "ramos",
    "kim", "cox", "ward", "richardson", "watson", "brooks", "chavez",
    "wood", "james", "bennett", "gray", "mendoza", "ruiz", "hughes",
    "price", "alvarez", "castillo", "sanders", "patel", "myers", "long",
    "ross", "foster", "jimenez"
]

STREETS = [
    "main st", "oak ave", "elm st", "maple dr", "cedar ln", "pine rd",
    "washington blvd", "park ave", "lake dr", "hill st", "forest rd",
    "river rd", "sunset blvd", "highland ave", "valley rd", "spring st",
    "meadow ln", "cherry ln", "walnut st", "birch dr", "poplar ave",
    "willow way", "aspen ct", "juniper rd", "magnolia blvd"
]

CITIES = [
    "riverside", "los angeles", "san diego", "san francisco", "sacramento",
    "fresno", "long beach", "oakland", "bakersfield", "anaheim",
    "santa ana", "irvine", "chula vista", "stockton", "fremont",
    "moreno valley", "fontana", "modesto", "glendale", "huntington beach"
]

SPECIALTIES = [
    "engine repair", "transmission", "brakes", "electrical systems",
    "suspension", "exhaust systems", "air conditioning", "oil change",
    "tire service", "body work", "diagnostics", "fuel systems",
    "steering", "cooling systems", "general maintenance"
]

CAR_MAKES_MODELS = {
    "honda": ["civic", "accord", "cr-v", "pilot", "fit", "odyssey"],
    "toyota": ["camry", "corolla", "rav4", "highlander", "prius", "tacoma"],
    "ford": ["f-150", "mustang", "escape", "explorer", "focus", "fusion"],
    "chevrolet": ["silverado", "malibu", "equinox", "cruze", "tahoe", "camaro"],
    "nissan": ["altima", "sentra", "rogue", "pathfinder", "maxima", "frontier"],
    "bmw": ["3 series", "5 series", "x3", "x5", "7 series"],
    "mercedes": ["c-class", "e-class", "s-class", "glc", "gle"],
    "hyundai": ["elantra", "sonata", "tucson", "santa fe", "accent"],
    "kia": ["optima", "sorento", "sportage", "forte", "soul"],
    "subaru": ["outback", "forester", "impreza", "wrx", "crosstrek"],
    "volkswagen": ["jetta", "passat", "tiguan", "golf", "atlas"],
    "mazda": ["mazda3", "mazda6", "cx-5", "cx-9", "miata"],
}

COMPLAINTS = [
    "engine is making a weird noise",
    "check engine light came on",
    "brakes feel spongy",
    "car pulls to the left when braking",
    "ac is blowing warm air",
    "transmission slipping between gears",
    "weird vibration at highway speeds",
    "oil leak under the car",
    "battery keeps dying overnight",
    "steering wheel shakes when braking",
    "exhaust is louder than usual",
    "car overheating in traffic",
    "power windows stopped working",
    "squealing noise when turning",
    "headlights are dim",
    "car wont start in the morning",
    "rough idle at stoplights",
    "coolant warning light on",
    "grinding noise from rear wheels",
    "fuel efficiency dropped a lot",
    "windshield wipers not working",
    "door lock is stuck",
    "trunk wont open",
    "radio cuts in and out",
    "smell of burning rubber",
    "suspension feels bouncy",
    "clutch pedal feels loose",
    "smoke coming from under hood",
    "abs light is on",
    "tire pressure warning keeps coming back",
]

CLOSE_COMMENTS = [
    "replaced the part, good to go",
    "fixed the issue, test drove it",
    "cleaned and adjusted, should be fine now",
    "swapped out the old one for a new unit",
    "topped off fluids and tightened everything",
    "patched it up, recommend replacing soon",
    "full replacement done, runs smooth",
    "diagnosed and repaired, no further issues",
    "minor fix, just needed an adjustment",
    "replaced worn components, all good",
    "flushed the system and refilled",
    "realigned and balanced, drives straight now",
    "electrical issue fixed, tested all circuits",
    "filter and fluid change, should last a while",
    "welded the crack, structurally sound now",
]


def random_phone():
    """generate phone in (###)###-#### format."""
    digits = [random.randint(0, 9) for _ in range(10)]
    return "({}{}{}){}{}{}-{}{}{}{}".format(*digits)


def random_vin():
    """generate a 16-char alphanumeric vin."""
    return ''.join(random.choices(string.ascii_uppercase + string.digits, k=16))


def random_address():
    num = random.randint(100, 9999)
    street = random.choice(STREETS)
    city = random.choice(CITIES)
    return f"{num} {street}, {city}, CA {random.randint(90000, 96000)}"


def random_date(start_year=2020, end_year=2025):
    start = date(start_year, 1, 1)
    end = date(end_year, 12, 31)
    delta = (end - start).days
    return start + timedelta(days=random.randint(0, delta))


def write_csv(filename, header, rows):
    path = os.path.join(OUT_DIR, filename)
    with open(path, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(header)
        writer.writerows(rows)
    print(f"  wrote {len(rows)} rows to {filename}")


# --- generate customers ---
print("generating customers...")
customers = []
for i in range(500):
    fname = random.choice(FIRST_NAMES)
    lname = random.choice(LAST_NAMES)
    phone = random_phone()
    address = random_address()
    customers.append((i, fname, lname, phone, address))

write_csv("customer.csv", ["id", "fname", "lname", "phone", "address"], customers)

# --- generate mechanics ---
print("generating mechanics...")
mechanics = []
for i in range(250):
    fname = random.choice(FIRST_NAMES)
    lname = random.choice(LAST_NAMES)
    experience = random.randint(0, 40)
    specialty = random.choice(SPECIALTIES)
    mechanics.append((i, fname, lname, experience, specialty))

write_csv("mechanic.csv", ["id", "fname", "lname", "experience", "specialty"], mechanics)

# --- generate cars ---
print("generating cars...")
cars = []
used_vins = set()
for _ in range(600):
    vin = random_vin()
    while vin in used_vins:
        vin = random_vin()
    used_vins.add(vin)
    make = random.choice(list(CAR_MAKES_MODELS.keys()))
    model = random.choice(CAR_MAKES_MODELS[make])
    # spread years out, make sure some are pre-1995 for query 8
    year = random.randint(1985, 2024)
    cars.append((vin, make, model, year))

write_csv("car.csv", ["vin", "make", "model", "year"], cars)

# --- generate owns ---
# need some customers with >20 cars for query 7
print("generating ownership records...")
owns = []
car_index = 0
ownership_id = 0

# first, give 5 customers 22-25 cars each (for query 7)
heavy_owners = list(range(5))
for cid in heavy_owners:
    num_cars = random.randint(22, 25)
    for _ in range(num_cars):
        if car_index >= len(cars):
            break
        owns.append((ownership_id, cid, cars[car_index][0]))
        ownership_id += 1
        car_index += 1

# distribute remaining cars among other customers
remaining_customers = list(range(5, 500))
while car_index < len(cars):
    cid = random.choice(remaining_customers)
    owns.append((ownership_id, cid, cars[car_index][0]))
    ownership_id += 1
    car_index += 1

# some customers get extra cars (generate more cars for them)
for _ in range(200):
    vin = random_vin()
    while vin in used_vins:
        vin = random_vin()
    used_vins.add(vin)
    make = random.choice(list(CAR_MAKES_MODELS.keys()))
    model = random.choice(CAR_MAKES_MODELS[make])
    year = random.randint(1985, 2024)
    cars.append((vin, make, model, year))
    cid = random.choice(remaining_customers[:100])
    owns.append((ownership_id, cid, vin))
    ownership_id += 1

# rewrite car.csv with the extra cars
write_csv("car.csv", ["vin", "make", "model", "year"], cars)
write_csv("owns.csv", ["ownership_id", "customer_id", "car_vin"], owns)

# build lookup: customer_id -> list of vins they own
customer_cars = {}
for _, cid, vin in owns:
    customer_cars.setdefault(cid, []).append(vin)

# --- generate service requests ---
print("generating service requests...")
service_requests = []

# make sure some pre-1995 cars with low odometer get service requests (for query 8)
old_cars = [(vin, make, model, year) for vin, make, model, year in cars if year < 1995]

for rid in range(1500):
    # pick a random customer who owns at least one car
    cid = random.choice([c for c in customer_cars if customer_cars[c]])
    vin = random.choice(customer_cars[cid])

    # for first 50 requests, use old cars with low odometer (query 8)
    if rid < 50 and old_cars:
        old_car = random.choice(old_cars)
        vin = old_car[0]
        # find owner of this car
        for oid, owner_cid, owner_vin in owns:
            if owner_vin == vin:
                cid = owner_cid
                break
        odometer = random.randint(10000, 49000)
    else:
        odometer = random.randint(5000, 250000)

    sr_date = random_date()
    complain = random.choice(COMPLAINTS)
    service_requests.append((rid, cid, vin, sr_date.strftime("%Y-%m-%d"), odometer, complain))

write_csv("service_request.csv",
          ["rid", "customer_id", "car_vin", "date", "odometer", "complain"],
          service_requests)

# --- generate closed requests ---
print("generating closed requests...")
closed_requests = []

# close about 1000 of the 1500 service requests
closed_rids = random.sample(range(1500), 1000)
closed_rids.sort()

for wid, rid in enumerate(closed_rids):
    mid = random.randint(0, 249)
    # close date is same day or a few days after the request date
    sr_date_str = service_requests[rid][3]
    sr_year, sr_month, sr_day = sr_date_str.split("-")
    sr_date = date(int(sr_year), int(sr_month), int(sr_day))
    close_date = sr_date + timedelta(days=random.randint(0, 14))
    comment = random.choice(CLOSE_COMMENTS)
    # bills: mostly under 500, some over 100 (for query 6 filter)
    bill = round(random.uniform(25.0, 800.0), 2)
    closed_requests.append((wid, rid, mid, close_date.strftime("%Y-%m-%d"), comment, bill))

write_csv("closed_request.csv",
          ["wid", "rid", "mid", "date", "comment", "bill"],
          closed_requests)

print("\ndone! generated all csv files in data/")

# quick sanity checks
print(f"\n--- sanity checks ---")
print(f"customers: {len(customers)}")
print(f"mechanics: {len(mechanics)}")
print(f"cars: {len(cars)}")
print(f"owns: {len(owns)}")
print(f"service requests: {len(service_requests)}")
print(f"closed requests: {len(closed_requests)}")

# check query 7: customers with >20 cars
from collections import Counter
car_counts = Counter(cid for _, cid, _ in owns)
heavy = [(cid, cnt) for cid, cnt in car_counts.items() if cnt > 20]
print(f"customers with >20 cars: {len(heavy)} -> {heavy}")

# check query 8: pre-1995 cars with <50k odometer
car_years = {vin: year for vin, _, _, year in cars}
q8_count = sum(1 for _, _, vin, _, odo, _ in service_requests
               if car_years.get(vin, 2000) < 1995 and odo < 50000)
print(f"service requests on pre-1995 cars with <50k miles: {q8_count}")
