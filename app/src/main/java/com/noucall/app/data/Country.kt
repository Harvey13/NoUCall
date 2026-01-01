package com.noucall.app.data

import android.content.Context

data class Country(
    val name: String,
    val code: String,
    val prefix: String
) {
    override fun toString(): String {
        return "$prefix $name"
    }
}

object CountryData {
    private val countries = listOf(
        Country("France", "FR", "+33"),
        Country("Belgique", "BE", "+32"),
        Country("Suisse", "CH", "+41"),
        Country("Luxembourg", "LU", "+352"),
        Country("Allemagne", "DE", "+49"),
        Country("Autriche", "AT", "+43"),
        Country("Pays-Bas", "NL", "+31"),
        Country("Espagne", "ES", "+34"),
        Country("Italie", "IT", "+39"),
        Country("Portugal", "PT", "+351"),
        Country("Royaume-Uni", "GB", "+44"),
        Country("Irlande", "IE", "+353"),
        Country("Danemark", "DK", "+45"),
        Country("Suède", "SE", "+46"),
        Country("Norvège", "NO", "+47"),
        Country("Finlande", "FI", "+358"),
        Country("Islande", "IS", "+354"),
        Country("Grèce", "GR", "+30"),
        Country("Chypre", "CY", "+357"),
        Country("Malte", "MT", "+356"),
        Country("Estonie", "EE", "+372"),
        Country("Lettonie", "LV", "+371"),
        Country("Lituanie", "LT", "+370"),
        Country("Pologne", "PL", "+48"),
        Country("République Tchèque", "CZ", "+420"),
        Country("Slovaquie", "SK", "+421"),
        Country("Hongrie", "HU", "+36"),
        Country("Slovénie", "SI", "+386"),
        Country("Croatie", "HR", "+385"),
        Country("Bosnie-Herzégovine", "BA", "+387"),
        Country("Serbie", "RS", "+381"),
        Country("Monténégro", "ME", "+382"),
        Country("Macédoine du Nord", "MK", "+389"),
        Country("Bulgarie", "BG", "+359"),
        Country("Roumanie", "RO", "+40"),
        Country("Moldavie", "MD", "+373"),
        Country("Ukraine", "UA", "+380"),
        Country("Biélorussie", "BY", "+375"),
        Country("Russie", "RU", "+7"),
        Country("Turquie", "TR", "+90"),
        Country("Géorgie", "GE", "+995"),
        Country("Arménie", "AM", "+374"),
        Country("Azerbaïdjan", "AZ", "+994"),
        Country("Kazakhstan", "KZ", "+7"),
        Country("Ouzbékistan", "UZ", "+998"),
        Country("Tadjikistan", "TJ", "+992"),
        Country("Kirghizistan", "KG", "+996"),
        Country("Turkménistan", "TM", "+993"),
        Country("Afghanistan", "AF", "+93"),
        Country("Pakistan", "PK", "+92"),
        Country("Inde", "IN", "+91"),
        Country("Bangladesh", "BD", "+880"),
        Country("Sri Lanka", "LK", "+94"),
        Country("Népal", "NP", "+977"),
        Country("Bhoutan", "BT", "+975"),
        Country("Maldives", "MV", "+960"),
        Country("Myanmar", "MM", "+95"),
        Country("Thaïlande", "TH", "+66"),
        Country("Laos", "LA", "+856"),
        Country("Cambodge", "KH", "+855"),
        Country("Viêt Nam", "VN", "+84"),
        Country("Malaisie", "MY", "+60"),
        Country("Singapour", "SG", "+65"),
        Country("Brunei", "BN", "+673"),
        Country("Philippines", "PH", "+63"),
        Country("Indonésie", "ID", "+62"),
        Country("Timor-Leste", "TL", "+670"),
        Country("Chine", "CN", "+86"),
        Country("Hong Kong", "HK", "+852"),
        Country("Macao", "MO", "+853"),
        Country("Taïwan", "TW", "+886"),
        Country("Corée du Sud", "KR", "+82"),
        Country("Corée du Nord", "KP", "+850"),
        Country("Japon", "JP", "+81"),
        Country("Mongolie", "MN", "+976"),
        Country("Australie", "AU", "+61"),
        Country("Nouvelle-Zélande", "NZ", "+64"),
        Country("Fidji", "FJ", "+679"),
        Country("Papouasie-Nouvelle-Guinée", "PG", "+675"),
        Country("Vanuatu", "VU", "+678"),
        Country("Îles Salomon", "SB", "+677"),
        Country("Samoa", "WS", "+685"),
        Country("Kiribati", "KI", "+686"),
        Country("Tonga", "TO", "+676"),
        Country("Tuvalu", "TV", "+688"),
        Country("Nauru", "NR", "+674"),
        Country("Palaos", "PW", "+680"),
        Country("Îles Marshall", "MH", "+692"),
        Country("États-Unis", "US", "+1"),
        Country("Canada", "CA", "+1"),
        Country("Mexique", "MX", "+52"),
        Country("Guatemala", "GT", "+502"),
        Country("Belize", "BZ", "+501"),
        Country("Honduras", "HN", "+504"),
        Country("Salvador", "SV", "+503"),
        Country("Nicaragua", "NI", "+505"),
        Country("Costa Rica", "CR", "+506"),
        Country("Panama", "PA", "+507"),
        Country("Cuba", "CU", "+53"),
        Country("Jamaïque", "JM", "+1876"),
        Country("Haïti", "HT", "+509"),
        Country("République Dominicaine", "DO", "+1809"),
        Country("Porto Rico", "PR", "+1787"),
        Country("Trinité-et-Tobago", "TT", "+1868"),
        Country("Barbade", "BB", "+1246"),
        Country("Bahamas", "BS", "+1242"),
        Country("Grenade", "GD", "+1473"),
        Country("Sainte-Lucie", "LC", "+1758"),
        Country("Dominique", "DM", "+1767"),
        Country("Saint-Vincent", "VC", "+1784"),
        Country("Antigua-et-Barbuda", "AG", "+1268"),
        Country("Saint-Christophe", "KN", "+1869"),
        Country("Argentine", "AR", "+54"),
        Country("Brésil", "BR", "+55"),
        Country("Chili", "CL", "+56"),
        Country("Uruguay", "UY", "+598"),
        Country("Paraguay", "PY", "+595"),
        Country("Bolivie", "BO", "+591"),
        Country("Pérou", "PE", "+51"),
        Country("Équateur", "EC", "+593"),
        Country("Colombie", "CO", "+57"),
        Country("Venezuela", "VE", "+58"),
        Country("Guyana", "GY", "+592"),
        Country("Suriname", "SR", "+597"),
        Country("Afrique du Sud", "ZA", "+27"),
        Country("Namibie", "NA", "+264"),
        Country("Botswana", "BW", "+267"),
        Country("Zimbabwe", "ZW", "+263"),
        Country("Zambie", "ZM", "+260"),
        Country("Malawi", "MW", "+265"),
        Country("Mozambique", "MZ", "+258"),
        Country("Eswatini", "SZ", "+268"),
        Country("Lesotho", "LS", "+266"),
        Country("Angola", "AO", "+244"),
        Country("République Démocratique du Congo", "CD", "+243"),
        Country("République du Congo", "CG", "+242"),
        Country("Gabon", "GA", "+241"),
        Country("Guinée Équatoriale", "GQ", "+240"),
        Country("Cameroun", "CM", "+237"),
        Country("République Centrafricaine", "CF", "+236"),
        Country("Tchad", "TD", "+235"),
        Country("Soudan", "SD", "+249"),
        Country("Soudan du Sud", "SS", "+211"),
        Country("Érythrée", "ER", "+291"),
        Country("Djibouti", "DJ", "+253"),
        Country("Somalie", "SO", "+252"),
        Country("Éthiopie", "ET", "+251"),
        Country("Kenya", "KE", "+254"),
        Country("Ouganda", "UG", "+256"),
        Country("Tanzanie", "TZ", "+255"),
        Country("Rwanda", "RW", "+250"),
        Country("Burundi", "BI", "+257"),
        Country("Nigeria", "NG", "+234"),
        Country("Niger", "NE", "+227"),
        Country("Mali", "ML", "+223"),
        Country("Burkina Faso", "BF", "+226"),
        Country("Côte d'Ivoire", "CI", "+225"),
        Country("Guinée", "GN", "+224"),
        Country("Sierra Leone", "SL", "+232"),
        Country("Libéria", "LR", "+231"),
        Country("Ghana", "GH", "+233"),
        Country("Togo", "TG", "+228"),
        Country("Bénin", "BJ", "+229"),
        Country("Mauritanie", "MR", "+222"),
        Country("Sénégal", "SN", "+221"),
        Country("Gambie", "GM", "+220"),
        Country("Guinée-Bissau", "GW", "+245"),
        Country("Cap-Vert", "CV", "+238"),
        Country("Sao Tomé-et-Principe", "ST", "+239"),
        Country("Égypte", "EG", "+20"),
        Country("Libye", "LY", "+218"),
        Country("Tunisie", "TN", "+216"),
        Country("Algérie", "DZ", "+213"),
        Country("Maroc", "MA", "+212"),
        Country("Sahara Occidental", "EH", "+212"),
        Country("Maurice", "MU", "+230"),
        Country("Seychelles", "SC", "+248"),
        Country("Comores", "KM", "+269"),
        Country("Madagascar", "MG", "+261"),
        Country("Mayotte", "YT", "+262"),
        Country("Réunion", "RE", "+262"),
        Country("Île Maurice", "MU", "+230")
    )

    fun getAllCountries(): List<Country> = countries

    fun searchCountries(query: String): List<Country> {
        val lowerQuery = query.lowercase()
        return countries.filter { country ->
            country.name.lowercase().contains(lowerQuery) ||
            country.prefix.contains(query) ||
            country.code.lowercase().contains(lowerQuery)
        }
    }

    fun findCountryByName(name: String): Country? {
        return countries.find { it.name.equals(name, ignoreCase = true) }
    }

    fun findCountryByPrefix(prefix: String): Country? {
        return countries.find { it.prefix == prefix }
    }

    // New localized functions
    fun getAllCountries(context: Context): List<Country> {
        return countries.map { country ->
            val countryKey = getCountryKey(country.name)
            val nameResId = context.resources.getIdentifier(countryKey, "string", context.packageName)
            val localized_name = if (nameResId != 0) context.getString(nameResId) else country.name
            Country(localized_name, country.code, country.prefix)
        }
    }

    private fun getCountryKey(frenchName: String): String {
        return when (frenchName) {
            "France" -> "country_france"
            "Belgique" -> "country_belgium"
            "Suisse" -> "country_switzerland"
            "Luxembourg" -> "country_luxembourg"
            "Allemagne" -> "country_germany"
            "Autriche" -> "country_austria"
            "Pays-Bas" -> "country_netherlands"
            "Espagne" -> "country_spain"
            "Italie" -> "country_italy"
            "Portugal" -> "country_portugal"
            "Royaume-Uni" -> "country_united_kingdom"
            "Irlande" -> "country_ireland"
            "Danemark" -> "country_denmark"
            "Suède" -> "country_sweden"
            "Norvège" -> "country_norway"
            "Finlande" -> "country_finland"
            "Islande" -> "country_iceland"
            "Grèce" -> "country_greece"
            "Chypre" -> "country_cyprus"
            "Malte" -> "country_malta"
            "Estonie" -> "country_estonia"
            "Lettonie" -> "country_latvia"
            "Lituanie" -> "country_lithuania"
            "Pologne" -> "country_poland"
            "République Tchèque" -> "country_czech_republic"
            "Slovaquie" -> "country_slovakia"
            "Hongrie" -> "country_hungary"
            "Slovénie" -> "country_slovenia"
            "Croatie" -> "country_croatia"
            "Bosnie-Herzégovine" -> "country_bosnia_herzegovina"
            "Serbie" -> "country_serbia"
            "Monténégro" -> "country_montenegro"
            "Macédoine du Nord" -> "country_north_macedonia"
            "Bulgarie" -> "country_bulgaria"
            "Roumanie" -> "country_romania"
            "Moldavie" -> "country_moldova"
            "Ukraine" -> "country_ukraine"
            "Biélorussie" -> "country_belarus"
            "Russie" -> "country_russia"
            "Turquie" -> "country_turkey"
            "Géorgie" -> "country_georgia"
            "Arménie" -> "country_armenia"
            "Azerbaïdjan" -> "country_azerbaijan"
            "Kazakhstan" -> "country_kazakhstan"
            "Ouzbékistan" -> "country_uzbekistan"
            "Tadjikistan" -> "country_tajikistan"
            "Kirghizistan" -> "country_kyrgyzstan"
            "Turkménistan" -> "country_turkmenistan"
            "Afghanistan" -> "country_afghanistan"
            "Pakistan" -> "country_pakistan"
            "Inde" -> "country_india"
            "Bangladesh" -> "country_bangladesh"
            "Sri Lanka" -> "country_sri_lanka"
            "Népal" -> "country_nepal"
            "Bhoutan" -> "country_bhutan"
            "Maldives" -> "country_maldives"
            "Myanmar" -> "country_myanmar"
            "Thaïlande" -> "country_thailand"
            "Laos" -> "country_laos"
            "Cambodge" -> "country_cambodia"
            "Viêt Nam" -> "country_vietnam"
            "Malaisie" -> "country_malaysia"
            "Singapour" -> "country_singapore"
            "Brunei" -> "country_brunei"
            "Philippines" -> "country_phippines"
            "Indonésie" -> "country_indonesia"
            "Timor-Leste" -> "country_timor_leste"
            "Chine" -> "country_china"
            "Hong Kong" -> "country_hong_kong"
            "Macao" -> "country_macau"
            "Taïwan" -> "country_taiwan"
            "Corée du Sud" -> "country_south_korea"
            "Corée du Nord" -> "country_north_korea"
            "Japon" -> "country_japan"
            "Mongolie" -> "country_mongolia"
            "Australie" -> "country_australia"
            "Nouvelle-Zélande" -> "country_new_zealand"
            "Fidji" -> "country_fiji"
            "Papouasie-Nouvelle-Guinée" -> "country_papua_new_guinea"
            "Vanuatu" -> "country_vanuatu"
            "Îles Salomon" -> "country_solomon_islands"
            "Samoa" -> "country_samoa"
            "Kiribati" -> "country_kiribati"
            "Tonga" -> "country_tonga"
            "Tuvalu" -> "country_tuvalu"
            "Nauru" -> "country_nauru"
            "Palaos" -> "country_palau"
            "Îles Marshall" -> "country_marshall_islands"
            "États-Unis" -> "country_united_states"
            "Canada" -> "country_canada"
            "Mexique" -> "country_mexico"
            "Guatemala" -> "country_guatemala"
            "Belize" -> "country_belize"
            "Honduras" -> "country_honduras"
            "Salvador" -> "country_el_salvador"
            "Nicaragua" -> "country_nicaragua"
            "Costa Rica" -> "country_costa_rica"
            "Panama" -> "country_panama"
            "Cuba" -> "country_cuba"
            "Jamaïque" -> "country_jamaica"
            "Haïti" -> "country_haiti"
            "République Dominicaine" -> "country_dominican_republic"
            "Porto Rico" -> "country_puerto_rico"
            "Trinité-et-Tobago" -> "country_trinidad_tobago"
            "Barbade" -> "country_barbados"
            "Bahamas" -> "country_bahamas"
            "Grenade" -> "country_grenada"
            "Sainte-Lucie" -> "country_saint_lucia"
            "Dominique" -> "country_dominica"
            "Saint-Vincent" -> "country_saint_vincent"
            "Antigua-et-Barbuda" -> "country_antigua_barbuda"
            "Saint-Christophe" -> "country_saint_kitts"
            "Argentine" -> "country_argentina"
            "Brésil" -> "country_brazil"
            "Chili" -> "country_chile"
            "Uruguay" -> "country_uruguay"
            "Paraguay" -> "country_paraguay"
            "Bolivie" -> "country_bolivia"
            "Pérou" -> "country_peru"
            "Équateur" -> "country_ecuador"
            "Colombie" -> "country_colombia"
            "Venezuela" -> "country_venezuela"
            "Guyana" -> "country_guyana"
            "Suriname" -> "country_suriname"
            "Afrique du Sud" -> "country_south_africa"
            "Namibie" -> "country_namibia"
            "Botswana" -> "country_botswana"
            "Zimbabwe" -> "country_zimbabwe"
            "Zambie" -> "country_zambia"
            "Malawi" -> "country_malawi"
            "Mozambique" -> "country_mozambique"
            "Eswatini" -> "country_eswatini"
            "Lesotho" -> "country_lesotho"
            "Angola" -> "country_angola"
            "République Démocratique du Congo" -> "country_democratic_republic_congo"
            "République du Congo" -> "country_republic_congo"
            "Gabon" -> "country_gabon"
            "Guinée Équatoriale" -> "country_equatorial_guinea"
            "Cameroun" -> "country_cameroon"
            "République Centrafricaine" -> "country_central_african_republic"
            "Tchad" -> "country_chad"
            "Soudan" -> "country_sudan"
            "Soudan du Sud" -> "country_south_sudan"
            "Érythrée" -> "country_eritrea"
            "Djibouti" -> "country_djibouti"
            "Somalie" -> "country_somalia"
            "Éthiopie" -> "country_ethiopia"
            "Kenya" -> "country_kenya"
            "Ouganda" -> "country_uganda"
            "Tanzanie" -> "country_tanzania"
            "Rwanda" -> "country_rwanda"
            "Burundi" -> "country_burundi"
            "Nigeria" -> "country_nigeria"
            "Niger" -> "country_niger"
            "Mali" -> "country_mali"
            "Burkina Faso" -> "country_burkina_faso"
            "Côte d'Ivoire" -> "country_cote_divoire"
            "Guinée" -> "country_guinea"
            "Sierra Leone" -> "country_sierra_leone"
            "Libéria" -> "country_liberia"
            "Ghana" -> "country_ghana"
            "Togo" -> "country_togo"
            "Bénin" -> "country_benin"
            "Mauritanie" -> "country_mauritania"
            "Sénégal" -> "country_senegal"
            "Gambie" -> "country_gambia"
            "Guinée-Bissau" -> "country_guinea_bissau"
            "Cap-Vert" -> "country_cape_verde"
            "Sao Tomé-et-Principe" -> "country_sao_tome_principe"
            "Égypte" -> "country_egypt"
            "Libye" -> "country_libya"
            "Tunisie" -> "country_tunisia"
            "Algérie" -> "country_algeria"
            "Maroc" -> "country_morocco"
            "Sahara Occidental" -> "country_western_sahara"
            "Maurice" -> "country_mauritius"
            "Seychelles" -> "country_seychelles"
            "Comores" -> "country_comoros"
            "Madagascar" -> "country_madagascar"
            "Mayotte" -> "country_mayotte"
            "Réunion" -> "country_reunion"
            else -> "country_${frenchName.lowercase().replace(" ", "_").replace("-", "_").replace("é", "e").replace("è", "e").replace("ê", "e").replace("à", "a").replace("â", "a").replace("î", "i").replace("ï", "i").replace("ô", "o").replace("ö", "o").replace("ù", "u").replace("û", "u").replace("ç", "c")}"
        }
    }

    fun searchCountries(context: Context, query: String): List<Country> {
        val countries = getAllCountries(context)
        val lowerQuery = query.lowercase()
        return countries.filter { country ->
            country.name.lowercase().contains(lowerQuery) ||
            country.prefix.contains(query) ||
            country.code.lowercase().contains(lowerQuery)
        }
    }

    fun findCountryByName(context: Context, name: String): Country? {
        val countries = getAllCountries(context)
        return countries.find { it.name.equals(name, ignoreCase = true) }
    }

    fun findCountryByPrefix(context: Context, prefix: String): Country? {
        val countries = getAllCountries(context)
        return countries.find { it.prefix == prefix }
    }
}
