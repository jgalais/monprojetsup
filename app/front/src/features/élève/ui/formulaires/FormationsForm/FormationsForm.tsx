import { type FormationsFormProps, type SituationFormationsÉlève } from "./FormationsForm.interface";
import useFormationsForm from "./useFormationsForm";
import BoutonRadioRiche from "@/components/_dsfr/BoutonRadioRiche/BoutonRadioRiche";
import SélecteurMultiple from "@/components/SélecteurMultiple/SélecteurMultiple";
import { i18n } from "@/configuration/i18n/i18n";

const FormationsForm = ({ àLaSoumissionDuFormulaireAvecSuccès, formId }: FormationsFormProps) => {
  const {
    mettreÀJourÉlève,
    situationFormationsOptions,
    valeurSituationFormations,
    setValeurSituationFormations,
    auChangementDesFormationsSélectionnées,
    formationsSuggérées,
    formationsSélectionnéesParDéfaut,
    àLaRechercheDUneFormation,
    rechercheFormationsEnCours,
  } = useFormationsForm({ àLaSoumissionDuFormulaireAvecSuccès });

  return (
    <form
      id={formId}
      noValidate
      onSubmit={mettreÀJourÉlève}
    >
      <BoutonRadioRiche
        auChangementValeurSélectionnée={(valeur) => setValeurSituationFormations(valeur as SituationFormationsÉlève)}
        légende={i18n.ÉLÈVE.FORMATIONS.SITUATION.LÉGENDE}
        options={situationFormationsOptions}
      />
      {valeurSituationFormations === "quelques_pistes" && formationsSélectionnéesParDéfaut && (
        <div className="mt-12">
          <SélecteurMultiple
            auChangementOptionsSélectionnées={auChangementDesFormationsSélectionnées}
            description={i18n.ÉLÈVE.FORMATIONS.FORMATIONS_ENVISAGÉES.DESCRIPTION}
            label={i18n.ÉLÈVE.FORMATIONS.FORMATIONS_ENVISAGÉES.LABEL}
            optionsSuggérées={formationsSuggérées}
            optionsSélectionnéesParDéfaut={formationsSélectionnéesParDéfaut}
            rechercheSuggestionsEnCours={rechercheFormationsEnCours}
            texteOptionsSélectionnées={i18n.ÉLÈVE.FORMATIONS.FORMATIONS_ENVISAGÉES.SÉLECTIONNÉES}
            àLaRechercheDUneOption={àLaRechercheDUneFormation}
          />
        </div>
      )}
    </form>
  );
};

export default FormationsForm;
