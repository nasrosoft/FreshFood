# FreshFood — PC Master / Android Extensions

Client requirement: **the Windows PC is the master of the application; the Android apps are only extensions of it.**

```
                 ┌──────────────────────────────────────────┐
                 │  WINDOWS MASTER STATION (DesktopApp/)     │
                 │  • dashboard, orders, dispatch, products, │
                 │    stock entry, customers & credit        │
                 │  • creates driver accounts                │
                 │  • approves / revokes phones + modules    │
                 │  • branding, licence lock                 │
                 │  • heartbeat every 30 s                   │
                 └───────────────┬──────────────────────────┘
                                 │ same tables + RPCs
                       ┌─────────▼─────────┐
                       │  Supabase (shared  │  process_sale, sync_delivery_completion,
                       │  data channel)     │  process_purchase, register_device, ...
                       └─────────┬─────────┘
               ┌─────────────────┼─────────────────┐
        ┌──────▼──────┐   ┌──────▼──────┐   ┌──────▼──────┐
        │ Android #1  │   │ Android #2  │   │ Android #3  │   EXTENSIONS
        │ deliveries  │   │ deliveries  │   │ deliveries, │   (only the modules
        │             │   │             │   │ pos         │    the PC granted)
        └─────────────┘   └─────────────┘   └─────────────┘
```

**Why Supabase stays in the middle:** drivers work on the road, so a phone can't reach a PC on the shop's local network. The PC is the master in authority, not in transport. It decides who may connect and what each phone can do, and every business rule still runs in the shared stored procedures (FEFO, the LIVRÉE commit point, the credit ledger).

## What "master" means concretely

| Rule | Enforced by |
|---|---|
| Only one Windows install is the master (`app_settings.master_device_id`) | `register_device`, `claim_master` |
| The first Windows install signed in as **ADMIN** becomes master automatically | `register_device` |
| Every phone registers on login and stays **PENDING** until the PC approves it | `register_device` + Android `DeviceGate` |
| Approve / revoke / module changes are accepted **only from the master PC** | `set_device_access` (checks `is_master_device`) |
| `users`, `settings`, `devices` can never be granted to a phone | SQL filter in `set_device_access` / `set_extension_policy` |
| Disabling a staff account on the PC locks all of that person's phones | `register_device` returns `REVOKED` |
| Phones re-check every 30 s and on resume, so a revoke applies within about 30 s | Android `DeviceGate` |
| Phones show a "PC principal hors ligne" banner when the heartbeat is older than 2 min | `master_heartbeat` + `masterOnline` |
| Brand / licence settings can only be changed by admins (it used to be `USING (true)`) | RLS policy on `app_settings` |

Modules a phone can be granted: `deliveries`, `dashboard`, `pos`, `products`, `purchases`, `customers`, `inventory`, `returns`.

## Setup (once)

1. **Database**: run [`master_extension_architecture.sql`](master_extension_architecture.sql) in the Supabase SQL editor. The script is idempotent.
2. **Windows master**: run `DesktopApp/` (see below) and sign in with an **ADMIN** account. This PC becomes the master.
3. **Phones**: install the updated Android app. A driver signs in, the phone shows *En attente d'approbation* with an 8-character code, and the admin approves that code in **Appareils Android** on the PC.

Before step 1 runs, the Android app keeps working as before ("legacy mode": the RPC doesn't exist yet, so it falls back to role-based menus).

## Windows app (`DesktopApp/`)

Kotlin + Compose Multiplatform Desktop. It reuses the Android models and the same Supabase project.

```bash
cd DesktopApp
./gradlew run                    # run from source
./gradlew createDistributable    # portable folder: build/compose/binaries/main/app/FreshFoodMaster/FreshFoodMaster.exe
./gradlew packageMsi             # Windows installer (needs the WiX toolset installed)
```

JDK 17+ required (`JAVA_HOME` must point to a valid JDK).

Screens: Tableau de bord · Nouvelle commande (counter sale or dispatch to a driver) · Livraisons (live board, reassign, edit quantities, mark LIVRÉE, cancel) · Produits · Entrées de stock (FEFO lots) · Clients & crédit (repayments, history) · Livreurs (create / disable accounts) · Appareils Android (approve, revoke, modules) · Paramètres (brand, extension policy, licence lock).

The app keeps running in the system tray when the window is closed, so phones keep seeing the PC online. It shows Windows notifications when a driver delivers, when a delivery is cancelled, and when a new phone asks for access.

Local files live in `%APPDATA%\FreshFoodMaster\`:
- `device.properties`: identity of this PC (keep it; deleting it makes the PC a new device)
- `session.json`: signed-in session
- `config.properties` (optional): `supabase.url=` / `supabase.key=` to point at another project

**Replacing the PC:** install on the new PC, sign in as admin, then click *Faire de ce PC le poste principal* (`claim_master`).

## Android changes

- `presentation/activation/DeviceGate.kt`: post-login gate (registration, pending / revoked screens, 30 s re-check, offline cache)
- `ActivationRepository.registerDevice` + `domain/model/DeviceRegistration.kt`
- `MainAppScreen`: drawer, bottom bar and start screen are filtered by `allowedModules`; Users and Paramétrage are hidden on phones; offline banner
- New strings in EN / FR / AR

## Security note

Device approval is enforced in the app and in the RPCs. Table-level RLS still authorizes by **user role**, because Supabase JWTs don't carry a device id. Someone with valid credentials and a modified app could bypass the device gate. For a hard stop (for example a driver who left), also change that account's password or delete the user in the Supabase dashboard (Authentication → Users).
