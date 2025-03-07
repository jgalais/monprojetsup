import ConfirmationInscription from "@/features/élève/ui/inscription/ConfirmationInscription/ConfirmationInscription";
import { createLazyFileRoute } from "@tanstack/react-router";

export const Route = createLazyFileRoute("/_auth/eleve/_inscription/inscription/confirmation/")({
  component: ConfirmationInscription,
});
