import { type BacRepository } from "@/features/bac/infrastructure/bacRepository.interface";
import { bacInMemoryRepository } from "@/features/bac/infrastructure/gateway/bacInMemoryRepository/bacInMemoryRepository";
import { RechercherSpécialitésPourUnBacUseCase } from "@/features/bac/usecase/RechercherSpécialitésPourUnBac";
import { RécupérerBacsUseCase } from "@/features/bac/usecase/RécupérerBacs";
import { RécupérerSpécialitésUseCase } from "@/features/bac/usecase/RécupérerSpécialités";
import { type CentreIntêretRepository } from "@/features/centreIntêret/infrastructure/centreIntêretRepository.interface";
import { centreIntêretInMemoryRepository } from "@/features/centreIntêret/infrastructure/gateway/centreIntêretInMemoryRepository/centreIntêretInMemoryRepository";
import { RécupérerCentresIntêretsGroupésParCatégorieUseCase } from "@/features/centreIntêret/usecase/RécupérerCentresIntêretsGroupésParCatégorie";
import { type DomaineProfessionnelRepository } from "@/features/domaineProfessionnel/infrastructure/domaineProfessionnelRepository.interface";
import { domaineProfessionnelInMemoryRepository } from "@/features/domaineProfessionnel/infrastructure/gateway/domaineProfessionnelInMemoryRepository/domaineProfessionnelInMemoryRepository";
import { RécupérerDomainesProfessionnelsGroupésParCatégorieUseCase } from "@/features/domaineProfessionnel/usecase/RécupérerDomainesProfessionnelsGroupésParCatégorie";
import { type ÉlèveRepository } from "@/features/élève/infrastructure/gateway/élèveRepository.interface";
import { élèveSessionStorageRepository } from "@/features/élève/infrastructure/gateway/élèveSessionStorageRepository/élèveSessionStorageRepository";
import { CréerÉlèveUseCase } from "@/features/élève/usecase/CréerÉlève";
import { MettreÀJourÉlèveUseCase } from "@/features/élève/usecase/MettreÀJourÉlève";
import { RécupérerÉlèveUseCase } from "@/features/élève/usecase/RécupérerÉlève";
import { type FormationRepository } from "@/features/formation/infrastructure/formationRepository.interface";
import { formationInMemoryRepository } from "@/features/formation/infrastructure/gateway/formationInMemoryRepository/formationInMemoryRepository";
import { RécupérerFormationsUseCase } from "@/features/formation/usecase/RécupérerFormations";
import { métierInMemoryRepository } from "@/features/métier/infrastructure/gateway/métierInMemoryRepository/métierInMemoryRepository";
import { type MétierRepository } from "@/features/métier/infrastructure/métierRepository.interface";
import { RechercherMétiersUseCase } from "@/features/métier/usecase/RechercherMétiers";
import { RécupérerAperçusMétiersUseCase } from "@/features/métier/usecase/RécupérerAperçusMétiers";
import { RécupérerMétiersUseCase } from "@/features/métier/usecase/RécupérerMétiers";

export class Dépendances {
  private static instance: Dépendances;

  private readonly _élèveRepository: ÉlèveRepository;

  private readonly _formationRepository: FormationRepository;

  private readonly _métierRepository: MétierRepository;

  private readonly _bacRepository: BacRepository;

  private readonly _domaineProfessionnelRepository: DomaineProfessionnelRepository;

  private readonly _centreIntêretRepository: CentreIntêretRepository;

  public readonly créerÉlèveUseCase: CréerÉlèveUseCase;

  public readonly mettreÀJourÉlèveUseCase: MettreÀJourÉlèveUseCase;

  public readonly récupérerÉlèveUseCase: RécupérerÉlèveUseCase;

  public readonly récupérerFormationsUseCase: RécupérerFormationsUseCase;

  public readonly récupérerMétiersUseCase: RécupérerMétiersUseCase;

  public readonly récupérerAperçusMétiersUseCase: RécupérerAperçusMétiersUseCase;

  public readonly rechercherMétiersUseCase: RechercherMétiersUseCase;

  public readonly récupérerBacsUseCase: RécupérerBacsUseCase;

  public readonly récupérerSpécialitésUseCase: RécupérerSpécialitésUseCase;

  public readonly rechercherSpécialitésPourUnBacUseCase: RechercherSpécialitésPourUnBacUseCase;

  public readonly récupérerDomainesProfessionnelsGroupésParCatégorieUseCase: RécupérerDomainesProfessionnelsGroupésParCatégorieUseCase;

  public readonly récupérerCentresIntêretsGroupésParCatégorieUseCase: RécupérerCentresIntêretsGroupésParCatégorieUseCase;

  private constructor() {
    this._élèveRepository = new élèveSessionStorageRepository();
    this._formationRepository = new formationInMemoryRepository();
    this._métierRepository = new métierInMemoryRepository();
    this._bacRepository = new bacInMemoryRepository();
    this._domaineProfessionnelRepository = new domaineProfessionnelInMemoryRepository();
    this._centreIntêretRepository = new centreIntêretInMemoryRepository();

    this.créerÉlèveUseCase = new CréerÉlèveUseCase(this._élèveRepository);
    this.mettreÀJourÉlèveUseCase = new MettreÀJourÉlèveUseCase(this._élèveRepository);
    this.récupérerÉlèveUseCase = new RécupérerÉlèveUseCase(this._élèveRepository);
    this.récupérerFormationsUseCase = new RécupérerFormationsUseCase(this._formationRepository);
    this.récupérerMétiersUseCase = new RécupérerMétiersUseCase(this._métierRepository);
    this.récupérerAperçusMétiersUseCase = new RécupérerAperçusMétiersUseCase(this._métierRepository);
    this.rechercherMétiersUseCase = new RechercherMétiersUseCase(this._métierRepository);
    this.récupérerBacsUseCase = new RécupérerBacsUseCase(this._bacRepository);
    this.récupérerSpécialitésUseCase = new RécupérerSpécialitésUseCase(this._bacRepository);
    this.rechercherSpécialitésPourUnBacUseCase = new RechercherSpécialitésPourUnBacUseCase(this._bacRepository);
    this.récupérerDomainesProfessionnelsGroupésParCatégorieUseCase =
      new RécupérerDomainesProfessionnelsGroupésParCatégorieUseCase(this._domaineProfessionnelRepository);
    this.récupérerCentresIntêretsGroupésParCatégorieUseCase = new RécupérerCentresIntêretsGroupésParCatégorieUseCase(
      this._centreIntêretRepository,
    );
  }

  public static getInstance(): Dépendances {
    if (!Dépendances.instance) {
      Dépendances.instance = new Dépendances();
    }

    return Dépendances.instance;
  }
}

export const dépendances = Dépendances.getInstance();
