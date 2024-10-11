package org.opencdmp.filetransformer.rda.model.rda;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import gr.cite.tools.exception.MyApplicationException;

import java.util.HashMap;
import java.util.Map;

public enum Language {

	AAR("aar"),
	ABK("abk"),
	AFR("afr"),
	AKA("aka"),
	AMH("amh"),
	ARA("ara"),
	ARG("arg"),
	ASM("asm"),
	AVA("ava"),
	AVE("ave"),
	AYM("aym"),
	AZE("aze"),
	BAK("bak"),
	BAM("bam"),
	BEL("bel"),
	BEN("ben"),
	BIH("bih"),
	BIS("bis"),
	BOD("bod"),
	BOS("bos"),
	BRE("bre"),
	BUL("bul"),
	CAT("cat"),
	CES("ces"),
	CHA("cha"),
	CHE("che"),
	CHU("chu"),
	CHV("chv"),
	COR("cor"),
	COS("cos"),
	CRE("cre"),
	CYM("cym"),
	DAN("dan"),
	DEU("deu"),
	DIV("div"),
	DZO("dzo"),
	ELL("ell"),
	ENG("eng"),
	EPO("epo"),
	EST("est"),
	EUS("eus"),
	EWE("ewe"),
	FAO("fao"),
	FAS("fas"),
	FIJ("fij"),
	FIN("fin"),
	FRA("fra"),
	FRY("fry"),
	FUL("ful"),
	GLA("gla"),
	GLE("gle"),
	GLG("glg"),
	GLV("glv"),
	GRN("grn"),
	GUJ("guj"),
	HAT("hat"),
	HAU("hau"),
	HBS("hbs"),
	HEB("heb"),
	HER("her"),
	HIN("hin"),
	HMO("hmo"),
	HRV("hrv"),
	HUN("hun"),
	HYE("hye"),
	IBO("ibo"),
	IDO("ido"),
	III("iii"),
	IKU("iku"),
	ILE("ile"),
	INA("ina"),
	IND("ind"),
	IPK("ipk"),
	ISL("isl"),
	ITA("ita"),
	JAV("jav"),
	JPN("jpn"),
	KAL("kal"),
	KAN("kan"),
	KAS("kas"),
	KAT("kat"),
	KAU("kau"),
	KAZ("kaz"),
	KHM("khm"),
	KIK("kik"),
	KIN("kin"),
	KIR("kir"),
	KOM("kom"),
	KON("kon"),
	KOR("kor"),
	KUA("kua"),
	KUR("kur"),
	LAO("lao"),
	LAT("lat"),
	LAV("lav"),
	LIM("lim"),
	LIN("lin"),
	LIT("lit"),
	LTZ("ltz"),
	LUB("lub"),
	LUG("lug"),
	MAH("mah"),
	MAL("mal"),
	MAR("mar"),
	MKD("mkd"),
	MLG("mlg"),
	MLT("mlt"),
	MON("mon"),
	MRI("mri"),
	MSA("msa"),
	MYA("mya"),
	NAU("nau"),
	NAV("nav"),
	NBL("nbl"),
	NDE("nde"),
	NDO("ndo"),
	NEP("nep"),
	NLD("nld"),
	NNO("nno"),
	NOB("nob"),
	NOR("nor"),
	NYA("nya"),
	OCI("oci"),
	OJI("oji"),
	ORI("ori"),
	ORM("orm"),
	OSS("oss"),
	PAN("pan"),
	PLI("pli"),
	POL("pol"),
	POR("por"),
	PUS("pus"),
	QUE("que"),
	ROH("roh"),
	RON("ron"),
	RUN("run"),
	RUS("rus"),
	SAG("sag"),
	SAN("san"),
	SIN("sin"),
	SLK("slk"),
	SLV("slv"),
	SME("sme"),
	SMO("smo"),
	SNA("sna"),
	SND("snd"),
	SOM("som"),
	SOT("sot"),
	SPA("spa"),
	SQI("sqi"),
	SRD("srd"),
	SRP("srp"),
	SSW("ssw"),
	SUN("sun"),
	SWA("swa"),
	SWE("swe"),
	TAH("tah"),
	TAM("tam"),
	TAT("tat"),
	TEL("tel"),
	TGK("tgk"),
	TGL("tgl"),
	THA("tha"),
	TIR("tir"),
	TON("ton"),
	TSN("tsn"),
	TSO("tso"),
	TUK("tuk"),
	TUR("tur"),
	TWI("twi"),
	UIG("uig"),
	UKR("ukr"),
	URD("urd"),
	UZB("uzb"),
	VEN("ven"),
	VIE("vie"),
	VOL("vol"),
	WLN("wln"),
	WOL("wol"),
	XHO("xho"),
	YID("yid"),
	YOR("yor"),
	ZHA("zha"),
	ZHO("zho"),
	ZUL("zul");
	private final String value;
	private final static Map<String, Language> CONSTANTS = new HashMap<>();

	static {
		for (Language c: values()) {
			CONSTANTS.put(c.value, c);
		}
	}

	Language(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}

	@JsonValue
	public String value() {
		return this.value;
	}

	@JsonCreator
	public static Language fromValue(String value) {
		Language constant = CONSTANTS.get(value);
		if (constant == null) {
			throw new MyApplicationException(value);
		} else {
			return constant;
		}
	}

}
