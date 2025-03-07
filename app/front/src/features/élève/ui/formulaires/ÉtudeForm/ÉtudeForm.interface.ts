import { type AlternanceÉlève, type DuréeÉtudesPrévueÉlève } from "@/features/élève/domain/élève.interface";

export type ÉtudeFormProps = {
  formId: string;
  àLaSoumissionDuFormulaireAvecSuccès?: () => void;
};

export type DuréeÉtudesPrévueOptions = Array<{
  valeur: DuréeÉtudesPrévueÉlève;
  label: string;
}>;

export type AlternanceOptions = Array<{
  valeur: AlternanceÉlève;
  label: string;
}>;

export type useÉtudeFormArgs = {
  àLaSoumissionDuFormulaireAvecSuccès?: () => void;
};
